package eu.bmtv;

import java.util.LinkedList;
import java.util.List;

/**
 * Part d'un bonus mis à disposition pour le projet.
 */
public class Bonus implements Contrat {
    int date;
    double montant;

    public Bonus(int date, double montant) {
        this.date = date;
        this.montant = montant;
    }

    @Override
    public List<CashFlow> getCashFlows(int i) {
        final List<CashFlow> emptyList = new LinkedList<CashFlow>();
        if (i != date) {
            return emptyList;
        }
        List<CashFlow> cashFlows = new LinkedList<CashFlow>();
        cashFlows.add(CashFlow.newAllocation(montant));
        return cashFlows;
    }

    @Override
    public double getNAV(int i) { return 0.0; }
}
