[
    new MagicPermanentActivation(
        new MagicActivationHints(MagicTiming.Removal),
        "Destroy"
    ) {
        @Override
        public Iterable<MagicEvent> getCostEvent(final MagicPermanent source) {
            return [
                new MagicPayManaCostEvent(source,"{B}"),
                new MagicSacrificePermanentEvent(source, SACRIFICE_CREATURE)
            ];
        }

        @Override
        public MagicEvent getPermanentEvent(final MagicPermanent source,final MagicPayedCost payedCost) {
            return new MagicEvent(
                source,
                payedCost.getTarget(),
                this,
                "Destroy each creature with the same converted mana cost as RN."
            );
        }

        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            final MagicPlayer player = event.getPlayer();
            final int amount=event.getRefPermanent().getConvertedCost();
            game.logAppendMessage(player,"("+amount+")");
            final Collection<MagicPermanent> targets=
                game.filterPermanents(
                    player,
                    new MagicCMCPermanentFilter(
                        CREATURE,
                        Operator.EQUAL,
                        amount
                    )
                );
            game.doAction(new DestroyAction(targets));
        }
    }
]
