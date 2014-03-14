package eu.bmtv;

import java.util.LinkedList;
import java.util.List;

/**
 * Représentation d'un contrat d'emprunt.
  */
public class Emprunt implements Contrat {
    /** Emprunt lié à un project locatif. */
    boolean m_locatif = false;
    /** Date de début de l'emprunt. */
    int m_dateDebut = 1;
    /** capital emprunté */
    double capital;
    /** taux d'intérêt */
    double taux;
    /** durée en nombre de mois. */
    int duree;
    /** nombre de jours avant la première mensualité */
    int delaiInitial = 30;
    static int UN_MOIS = 30;

    private double loyer;
    private double[] interets;
    private double[] capitaux;

    public Emprunt(double capital, int dateDebut,
                   int duree, double taux, boolean locatif) {
        this.capital = capital;
        this.duree = duree;
        this.taux = taux;
        m_dateDebut = dateDebut;
        m_locatif = locatif;
        init();
    }

    private double getCapitalRembourse(double unLoyer) {
        double res = 0.0;
        int delai = delaiInitial;
        for (int i = 1; i <= duree; i++) {
            double ratio = delai/(double)UN_MOIS * taux/1200.0;
            double interets = (capital-res)*ratio;
            if (unLoyer < interets) {
                // le loyer ne couvre pas les intérêts
                return 0.0;
            }
            res += unLoyer - interets;
            if (res >= capital && i < duree) {
                return res + (duree - i) * unLoyer;
            }
            delai = UN_MOIS;
        }
        return res;
    }
    /**
     * Calcule la mensualité du prêt.
     */
    public double calculeLoyer() {
        double loyer = 0.0;
        double increment = 1000.0;
        double capitalRembourse = getCapitalRembourse(loyer);
        while (Math.abs(capitalRembourse - capital) > 0.01 && increment > 0.01) {
            if (capitalRembourse > capital) {
                loyer -= increment;
                increment = 0.5 * increment;
            }
            loyer += increment;
            capitalRembourse = getCapitalRembourse(loyer);
        }
        return loyer;
    }

    public static double arrondiAuCentime(double d) {
        return Math.round(d*100.0)/100.0;
    }

    /**
     * Calcule et enregistre les échéances et intérêts
     */
    public void init() {
        loyer = arrondiAuCentime(calculeLoyer());
        interets = new double[duree];
        capitaux = new double[duree];
        int delai = delaiInitial;
        double capitalRestant = capital;
        for (int i = 0; i < duree; i++) {
            interets[i] = capitalRestant * delai/(double)UN_MOIS * taux/1200.0;
            interets[i] = arrondiAuCentime(interets[i]);
            capitaux[i] = loyer - interets[i];
            capitalRestant -= capitaux[i];
            delai = UN_MOIS;
        }
        capitaux[duree-1] += capitalRestant;
    }

    /**
     * Renvoie le montant de la ième échéance
     * @param i
     * @return
     */
    public double getLoyer(int i) {
        return interets[i-1]+capitaux[i-1];
    }

    /**
     * Renvoie le montant des intérêts de la ième échéance
     * @param i
     * @return
     */
    public double getInterets(int i) {
        return interets[i-1];
    }

    /**
     * Retourne le montant total des intérêts
     */
    public double getTotalInterets() {
        double d = 0.0;
        for (int i = 0; i < duree; i++) {
            d += interets[i];
        }
        return d;
    }

    @Override
    public List<CashFlow> getCashFlows(int i) {
        List<CashFlow> cashFlows = new LinkedList<CashFlow>();
        int j = i - m_dateDebut + 1;
        if (j < 1 || j > duree) {
            return cashFlows;
        }
        if (j == 1) {
            cashFlows.add(CashFlow.newCashFlow(capital));
        }
        double loyer = getLoyer(j);
        double interets = getInterets(j);
        cashFlows.add(CashFlow.newRemboursementCapital(loyer - interets));
        cashFlows.add(CashFlow.newRemboursementInterets(interets, m_locatif));
        return cashFlows;
    }

    @Override
    public double getNAV(int i) {
        int j = i - m_dateDebut + 1;
        if (j < 1 || j > duree) {
            return 0.0;
        }
        double res = 0.0;
        for (int k = j; k <= duree; k++) {
            res += capitaux[k-1];
        }
        return 0.0;
    }
}
