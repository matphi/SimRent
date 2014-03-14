package eu.bmtv;

/**
 * Created with IntelliJ IDEA.
 * User: Propriétaire
 * Date: 23/01/14
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public interface Strategie {
    public void decide(Simulation simulation, int i);

    /**
     * Stratégie consistant à combler le deficit observé à chaque
     * période, dans la limite fixée.
     */
    public static class CombleDeficit implements Strategie {
        /** Limite des ressources par période. */
        private double m_limiteParPeriode;
        private double m_limite;
        private double m_cash;

        public CombleDeficit(double limiteParPeriode) {
            m_limiteParPeriode = limiteParPeriode;
        }

        @Override
        public void decide(Simulation simulation, int i) {
            if (i == 1) {
                m_limite = 0;
                m_cash = 0.0;
            }
            m_limite += m_limiteParPeriode;
            double net = CashFlow.sum(simulation.m_cashFlows[i-1], null);
            if (net + m_cash < -m_limite) {
                throw new RuntimeException("Comblement du deficit supérieur à la limite.");
            }
            if (net < 0.0) {
                double fromCash = Math.min(m_cash, -net);
                m_cash -= fromCash;
                net += fromCash;
                if (net < 0.0) {
                    System.out.println(i + ": alloc de " + (-net));
                    simulation.m_cashFlows[i-1].add(CashFlow.newAllocation(-net));
                    m_limite += net;
                }
            } else {
                m_cash += net;
            }
        }
    }
}
