[
    new MagicSpellCardEvent() {
        @Override
        public MagicEvent getEvent(final MagicCardOnStack cardOnStack,final MagicPayedCost payedCost) {
            return new MagicEvent(
                cardOnStack,
                MagicTargetChoice.NEG_TARGET_SPELL,
                this,
                "Counter target spell\$ unless its controller pays {1}. " +
                "That player discards a card."
            );
        }
        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            event.processTargetCardOnStack(game,new MagicCardOnStackAction() {
                public void doAction(final MagicCardOnStack targetSpell) {
                    game.addEvent(new MagicCounterUnlessEvent(event.getSource(),targetSpell,MagicManaCost.create("{1}")));
                    game.addEvent(new MagicDiscardEvent(event.getSource(),targetSpell.getController()));
                }
            });
        }
    }
]
