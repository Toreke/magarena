package magic.ui.screen.duel.mulligan;

import com.sun.glass.events.KeyEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import magic.data.MagicIcon;
import magic.model.MagicCardList;
import magic.ui.ScreenController;
import magic.translate.UiString;
import magic.ui.widget.cards.canvas.CardsCanvas.LayoutMode;
import magic.ui.widget.cards.canvas.CardsCanvas;
import magic.ui.widget.duel.choice.MulliganChoicePanel;
import magic.ui.screen.widget.MenuButton;
import magic.ui.WikiPage;
import magic.ui.helpers.KeyEventAction;
import magic.ui.screen.HeaderFooterScreen;

@SuppressWarnings("serial")
public class MulliganScreen extends HeaderFooterScreen {

    // translatable string
    private static final String _S1 = "Vancouver Mulligan";
    private static final String _S2 = "Close";
    private static final String _S3 = "Play this hand";
    private static final String _S5 = "Returns this hand to your library then draws a new hand with one less card.<br>You may scry 1 if your opening hand has fewer cards than your starting hand size.";

    private volatile static boolean isActive = false;

    private CardsCanvas cardsCanvas;
    private MulliganChoicePanel choicePanel;
    private final MagicCardList hand;

    public MulliganScreen(final MulliganChoicePanel choicePanel, final MagicCardList hand) {
        super(UiString.get(_S1));
        this.choicePanel = choicePanel;
        this.hand = hand;
        isActive = true;
        setMainContent(getScreenContent(hand));
        setHeaderContent(new HeaderPanel(choicePanel.getGameController().getGame()));
        setLeftFooter(MenuButton.build(this::doCancel, UiString.get(_S2)));
        setRightFooter(MenuButton.build(this::doNextAction, UiString.get(_S3)));
        addToFooter(MenuButton.build(this::doMulligan,
                MagicIcon.MULLIGAN, UiString.get(_S1), UiString.get(_S5))
        );
        setWikiPage(WikiPage.MULLIGAN);
        KeyEventAction.doAction(this, this::doCancel)
                .onFocus(0, KeyEvent.VK_ESCAPE);
    }

    public static boolean isActive() {
        return isActive;
    }

    private JPanel getScreenContent(final MagicCardList hand) {
        cardsCanvas = new CardsCanvas();
        cardsCanvas.setAnimationEnabled(true);
        cardsCanvas.setAnimationDelay(50, 20);
        cardsCanvas.setLayoutMode(LayoutMode.SCALE_TO_FIT);
        refreshCardsDisplay(hand);
        return cardsCanvas;
    }

    public void dealNewHand(final MulliganChoicePanel choicePanel, final MagicCardList hand) {
        this.choicePanel = choicePanel;
        refreshCardsDisplay(hand);
    }

    private void refreshCardsDisplay(final MagicCardList hand) {
        // Important: uses Runnable so painting works properly.
        SwingUtilities.invokeLater(() -> {
            cardsCanvas.refresh(hand);
        });
    }

    /**
     * Close screen and set flag so that it is not re-opened should the
     * user subsequently take a mulligan from the main game screen.
     */
    private void doCancel() {
        if (!cardsCanvas.isBusy()) {
            isActive = false;
            ScreenController.closeActiveScreen(false);
        }
    }

    private void doNextAction() {
        if (!cardsCanvas.isBusy()) {
            choicePanel.doMulliganAction(false);
            isActive = false;
            ScreenController.closeActiveScreen(false);
        }
    }

    private void doMulligan() {
        if (!cardsCanvas.isBusy()) {
            choicePanel.doMulliganAction(true);
            if (hand.size() <= 2) {
                isActive = false;
                ScreenController.closeActiveScreen(false);
            }
        }
    }
}
