package eu.bmtv;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Propriétaire
 * Date: 23/11/13
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public class BienImmobilier implements Contrat {
    /** La date d'achat du bien. */
    int m_dateAchat = 1;

    double prixAchat;
    /** montant des travaux de rénovation à l'achat */
    double prixTravauxReno;
    /** surface habitable du bien */
    double surface;
    /** taxe foncière à la date d'achat */
    double taxeFonciere;
    /** loyer hc annuel à la date d'achat */
    double loyerHc;
    /** charges annuelles récupérables à la date d'achat */
    double chargesRecuperables;
    /** charges annuelles non récupérables à la date d'achat hors taxes */
    double chargesNonRecuperables;

    public double getValo() {
        return prixAchat + prixTravauxReno;
    }

    @Override
    public List<CashFlow> getCashFlows(int i) {
        List<CashFlow> cashFlows = new LinkedList<CashFlow>();
        if (i < m_dateAchat) {
            return cashFlows;
        }
        // Travaux initiaux de rénovation
        if (i == m_dateAchat) {
            cashFlows.add(CashFlow.newCashFlow(-prixAchat));
            cashFlows.add(CashFlow.newChargeFonciere(prixTravauxReno, false));
        }
        // Loyer mensuel
        cashFlows.add(CashFlow.newRevenuFoncier(loyerHc / 12.0));
        cashFlows.add(CashFlow.newRecupChargeFonciere(chargesRecuperables / 12.0));
        // charges mensuelles
        cashFlows.add(CashFlow.newChargeFonciere(chargesRecuperables / 12.0, true));
        cashFlows.add(CashFlow.newChargeFonciere(chargesNonRecuperables / 12.0, false));
        // taxe foncière
        if (i % 11 == 0) {
            cashFlows.add(CashFlow.newImpot(taxeFonciere, true));
        }
        return cashFlows;
    }

    @Override
    public double getNAV(int i) {
        if (i < m_dateAchat) {
            return 0.0;
        }
        return getValo();
    }

}
