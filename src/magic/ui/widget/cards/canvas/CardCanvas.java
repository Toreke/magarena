package magic.ui.widget.cards.canvas;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import magic.exception.ScriptParseException;
import magic.model.IRenderableCard;
import magic.model.MagicCard;
import magic.model.MagicCardDefinition;
import magic.model.MagicPlayer;
import magic.ui.MagicImages;

final class CardCanvas {

    private Dimension cardSize;
    private Point position;
    private final Rectangle boundary = new Rectangle();
    private MagicCard card;
    private final IRenderableCard cardDef;

    public CardCanvas(final MagicCard card) {
        this.card = card;
        this.cardDef = card.getCardDefinition();
        this.cardSize = new Dimension();
        setPosition(new Point(0, 0));
    }

    public CardCanvas(final MagicCardDefinition cardDef) {
        try {
            this.card = new MagicCard(cardDef, MagicPlayer.NONE, 0);
        } catch (ScriptParseException ex) {
            this.card = new MagicCard(MagicCardDefinition.UNKNOWN, MagicPlayer.NONE, 0);
        }
        this.cardDef = cardDef;
        this.cardSize = new Dimension();
        setPosition(new Point(0, 0));
    }

    MagicCard getMagicCard() {
        return card;
    }

    public void setPosition(final Point p0) {
        position = p0;
        boundary.setBounds(position.x, position.y, cardSize.width, cardSize.height);
    }
    public Point getPosition() {
        return position;
    }

    public Dimension getSize() {
        return this.cardSize;
    }

    @Override
    public int hashCode() {
        final int hashcode1 = getFrontImage().hashCode();
        final int hashcode2 = getBackImage() == null ? 0 : getBackImage().hashCode();
        return 73 * hashcode1 ^ 79 * hashcode2;
    }

    public BufferedImage getFrontImage() {
        return MagicImages.getCardImage(cardDef);
    }

    public BufferedImage getBackImage() {
        return null;
    }

    Rectangle getBounds() {
        return new Rectangle(position, cardSize);
    }

    void setSize(int cardWidth, int cardHeight) {
        cardSize = new Dimension(cardWidth, cardHeight);
    }

}
