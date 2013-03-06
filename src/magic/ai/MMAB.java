package magic.ai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import magic.model.MagicGame;
import magic.model.MagicGameLog;
import magic.model.MagicPlayer;
import magic.model.event.MagicEvent;

public class MMAB implements MagicAI {
    
    private static final int INITIAL_MAX_DEPTH=120;
    private static final int INITIAL_MAX_GAMES=12000;
    private static final int         MAX_DEPTH=120;
    private static final int         MAX_GAMES=12000;

    private final int THREADS = Runtime.getRuntime().availableProcessors();
    
    private final boolean LOGGING;
    private final boolean CHEAT;
    private ArtificialPruneScore pruneScore = new ArtificialMultiPruneScore();

    MMAB() {
        //default: no logging, no cheats
        this(false, false);
    }
    
    MMAB(final boolean log, final boolean cheat) {
        LOGGING = log || (System.getProperty("debug") != null);
        CHEAT = cheat;
    }
    
    private void log(final String message) {
        MagicGameLog.log(message);
        if (LOGGING) {
            System.err.println(message);
        }
    }
    
    public Object[] findNextEventChoiceResults(final MagicGame sourceGame, final MagicPlayer scorePlayer) {
        final long startTime = System.currentTimeMillis();

        // copying the game is necessary because for some choices game scores might be calculated, 
        // find all possible choice results.
        MagicGame choiceGame = new MagicGame(sourceGame,scorePlayer);
        final MagicEvent event = choiceGame.getNextEvent();
        final List<Object[]> choices = event.getArtificialChoiceResults(choiceGame);
        final int size = choices.size();
        choiceGame = null;
        
        assert size != 0 : "ERROR: no choices available for MMAB";
        
        // single choice result.
        if (size == 1) {
            return sourceGame.map(choices.get(0));
        }
        
        // submit jobs
        final LinkedList<ArtificialWorker> workers = new LinkedList<ArtificialWorker>();
        final ArtificialScoreBoard scoreBoard = new ArtificialScoreBoard();
        final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        final List<ArtificialChoiceResults> achoices=new ArrayList<ArtificialChoiceResults>();
        final int artificialLevel = sourceGame.getArtificialLevel(scorePlayer.getIndex());
        final int mainPhases = artificialLevel;
        for (final Object[] choice : choices) {
            final ArtificialChoiceResults achoice=new ArtificialChoiceResults(choice);
            achoices.add(achoice);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final MagicGame workerGame=new MagicGame(sourceGame,scorePlayer);
                    if (!CHEAT) {
                        workerGame.setKnownCards();
                    }
                    workerGame.setFastChoices(true);
                    workerGame.setMainPhases(mainPhases);
                    final ArtificialWorker worker=new ArtificialWorker((int)Thread.currentThread().getId(),workerGame,scoreBoard);
                    worker.evaluateGame(achoice,getPruneScore(),MAX_DEPTH,MAX_GAMES);
                    updatePruneScore(achoice.aiScore.getScore());
                }
            });
        }
        executor.shutdown();
        try {
            // wait for 2 * artificialLevel seconds for jobs to finish
            executor.awaitTermination(artificialLevel * 2, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            // force termination of workers
            executor.shutdownNow();
        }
        
        // select the best scoring choice result.
        ArtificialScore bestScore = ArtificialScore.INVALID_SCORE;
        ArtificialChoiceResults bestAchoice = achoices.get(0);
        for (final ArtificialChoiceResults achoice : achoices) {
            if (bestScore.isBetter(achoice.aiScore,true)) {
                bestScore = achoice.aiScore;
                bestAchoice = achoice;                
            }
        }

        // Logging.
        final long timeTaken = System.currentTimeMillis() - startTime;
        log("MMAB" + 
            " index=" + scorePlayer.getIndex() +
            " life=" + scorePlayer.getLife() +
            " phase=" + sourceGame.getPhase().getType() + 
            " time=" + timeTaken + 
            " main=" + mainPhases);
        for (final ArtificialChoiceResults achoice : achoices) {
            log((achoice == bestAchoice ? "* " : "  ") + achoice);
        }

        return sourceGame.map(bestAchoice.choiceResults);
    }

    private void updatePruneScore(final int score) {
        pruneScore = pruneScore.getPruneScore(score,true);
    }
    
    private ArtificialPruneScore getPruneScore() {
        return pruneScore;
    }
}
