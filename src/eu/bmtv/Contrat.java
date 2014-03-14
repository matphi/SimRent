package eu.bmtv;

import java.util.LinkedList;
import java.util.List;

/**
 * Représentation d'un contrat produisant des cash flows.
 */
public interface Contrat {
    public List<CashFlow> getCashFlows(int i);

    public double getNAV(int i);

    /**
     * Un contrat représentant la résolution de mettre à disposition
     * une partie de revenus ne provenant pas de ceux générés par le
     * projet.
     */
    public static class RevenusMisADisposition implements Contrat {
        List<CashFlow> m_monthlyCashFlows = new LinkedList<CashFlow>();

        public RevenusMisADisposition(double montant) {
            m_monthlyCashFlows.add(CashFlow.newAllocation(montant));
        }

        @Override
        public List<CashFlow> getCashFlows(int i) {
            return m_monthlyCashFlows;
        }

        @Override
        public double getNAV(int i) { return 0.0; }
    }
}
