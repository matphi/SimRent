package eu.bmtv;

import eu.bmtv.CashFlow.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * Les dates sont représentées par des entiers désignant le numéro
 * de l'échéance depuis le début de la simulation.
 */
public class Simulation {
    int duree;
    List<Achat> achats = new LinkedList<Achat>();
    List<Dette> dettes = new LinkedList<Dette>();
    List<Bonus> boni = new LinkedList<Bonus>();
    /** Contrats produisant des cash flows. */
    List<Contrat> m_contrats = new LinkedList<Contrat>();
    /** Stratégies mises en oeuvre. */
    List<Strategie> m_strategies = new LinkedList<Strategie>();

    /**
     * Revenus annuels nets d'impôts disponibles pour l'épargne, y compris
     * l'investissement dans la résidence principale.
     */
    double revenusNonFonciersDispos;
    /**
     * Taux brut de rémunération du capital non investi dans l'immobilier.
     */
    double tauxFinancier;
    /**
     * Taux de la CSG, appliquée aux revenus fianciers et fonciers.
     */
    double tauxCSG = 15.5;
    /**
     * Taux marginal d'imposition.
     */
    double tauxMarginal = 14;
    /**
     * Deficit foncier max déductible par an.
     */
    double plafondDeficit = 10700;

    /** Listes des cash flows pour chaque période. */
    List<CashFlow>[] m_cashFlows;

    /** Revenus du travail mis à disposition pour le projet. */
    double[] revenusDispos;
    double[] revenusFonciers;
    double[] chargesFoncieres;
    double[] chargesFinancieres;
    double[] chargesFinancieresLocatives;
    double[] gainsFinanciers;
    double[] gainsFinanciersEpargne;
    /** Solde du compte projet. */
    double[] solde;
    /** Epargne. */
    double[] epargne;
    /** Taux de rémunération de l'épargne. */
    double tauxEpargne;
    private double[] m_reportsFonciers;

    protected void genereImpots(int i) {
        if (i % 12 != 0) {
            return;
        }
        int exercice = i / 12;
        // Rassemblement des cash flows de l'exercice
        List<CashFlow> cashFlows = new LinkedList<CashFlow>();
        for (int j = i-1; j >= 0 && j >= i-12; j--) {
            cashFlows.addAll(m_cashFlows[j]);
        }
        // Calcul des données
        double revenusFonciers = CashFlow.sum(cashFlows,
                CashFlow.PRED_REVENUS_FONCIERS);
        double chargesFoncieresNonRecuperables = CashFlow.sum(cashFlows,
                CashFlow.PRED_CHARGES_NON_RECUPERABLES);
        double chargesFoncieresFinancieres = CashFlow.sum(cashFlows,
                CashFlow.PRED_CHARGES_FONCIERES_FINANCIERES);
        double reportFoncierImputable = 0.0;
        double reportFoncierNonImputable = 0.0;

        System.out.println("Rev foncier pour exercice " + exercice + ": " + revenusFonciers);
        System.out.println("Charges foncières financières pour exercice " + exercice + ": " + chargesFoncieresFinancieres);
        System.out.println("Charges foncières pour exercice " + exercice + ": " + chargesFoncieresNonRecuperables);
        double netFoncier = revenusFonciers + chargesFoncieresNonRecuperables;
        System.out.println("Net foncier avant report pour exercice " + exercice + ": " + netFoncier);
        if (netFoncier < 0.0) {
            double deficitFinancier = - Math.min(0.0,
                    revenusFonciers - chargesFoncieresFinancieres);
            reportFoncierNonImputable += deficitFinancier;
            reportFoncierImputable += -netFoncier - deficitFinancier;
        } else {
            if (exercice > 10 && m_reportsFonciers[exercice-10] > 0.0) {
                System.out.println("PERTE DU DEFICIT EXERCICE-10: "
                    + m_reportsFonciers[exercice-10]);
            }
            for (int e = Math.max(1, exercice - 9);
                 e < exercice && netFoncier > 0; e++) {
                double r = Math.min(netFoncier, m_reportsFonciers[e]);
                if (r > 0.0) {
                    netFoncier -= r;
                    m_reportsFonciers[e] -= r;
                }
            }
        }
        System.out.println("Net foncier pour exercice " + exercice + ": " + netFoncier);
        double repriseNetImposable = 0.0;
        if (reportFoncierImputable > 0.0) {
            repriseNetImposable = Math.min(10700.0, reportFoncierImputable);
            reportFoncierImputable -= repriseNetImposable;
        }
        double netImposable;
        // Avantage fiscal
        if (repriseNetImposable > 0.0) {
            System.out.println("Reprise revenu imposable: " + repriseNetImposable);
            m_cashFlows[i-1].add(CashFlow.newReducImpot(repriseNetImposable * tauxMarginal / 100.0));
        }
        // impôt foncier
        m_cashFlows[i-1].add(CashFlow.newImpot(Math.max(netFoncier, 0.0) * (tauxCSG + tauxMarginal) / 100.0, false));
        m_reportsFonciers[exercice] = reportFoncierNonImputable + reportFoncierImputable;
    }

    public void calcule() {
        revenusDispos = new double[duree];
        revenusFonciers = new double[duree];
        chargesFoncieres = new double[duree];
        chargesFinancieres = new double[duree];
        chargesFinancieresLocatives = new double[duree];
        gainsFinanciers = new double[duree];
        gainsFinanciersEpargne = new double[duree];
        solde = new double[duree];
        epargne = new double[duree];
        m_cashFlows = new List[duree];
        m_reportsFonciers = new double[duree/12+1];
        double reportDeficitFoncier = 0.0;
        for (int i = 1; i <= duree; i++) {
            m_cashFlows[i-1] = new LinkedList<CashFlow>();
            // Application des différents contrats
            for (Contrat contrat: m_contrats) {
                m_cashFlows[i-1].addAll(contrat.getCashFlows(i));
            }
            // NEW: Application de la fiscalité
            genereImpots(i);
            // NEW: Décision
            for (Strategie strategie: m_strategies) {
                strategie.decide(this, i);
            }
            // OLD
            // revenus du travail mis à disposition pour le projet
            revenusDispos[i-1] = revenusNonFonciersDispos;
            epargne[i-1] = 0;
            for (Bonus bonus: boni) {
                if (bonus.date == i) {
                    epargne[i-1] += bonus.montant;
                }
            }
            // calcul chargesRecuperables financières sur la période
            for (Dette dette: dettes) {
                if (dette.dateDebut <= i
                    && i < dette.emprunt.duree + dette.dateDebut) {
                    double charge = dette.emprunt.getInterets(i - dette.dateDebut + 1);
                    chargesFinancieres[i-1] += dette.emprunt.getLoyer(i - dette.dateDebut + 1);
                    if (dette.locative) {
                        chargesFinancieresLocatives[i-1] += charge;
                    }
                }
            }
            // calcul revenus fonciers sur la période
            // calcul chargesRecuperables foncières sur la période
            for (Achat achat: achats) {
                if (achat.dateAchat <= i) {
                    revenusFonciers[i-1] += achat.appart.loyerHc/12.0
                    /*+ achat.appart.chargesRecuperables /12*/;
                    chargesFoncieres[i-1] += /*achat.appart.chargesRecuperables /12.0
                    + */achat.appart.taxeFonciere / 12.0;
                    if (i == achat.dateAchat) {
                        reportDeficitFoncier += achat.appart.prixTravauxReno;
                    }
                }
            }
            // Calcul des gains financiers et du solde
            if (i > 1) {
                gainsFinanciers[i-1] = Math.max(0.0,
                        solde[i-2]
                        * tauxFinancier / 1200.0);
                solde[i-1] += solde[i-2];
                gainsFinanciersEpargne[i-1] += epargne[i-2] * tauxEpargne / 1200.0;
                epargne[i-1] += epargne[i-2] + gainsFinanciersEpargne[i-1];
            }
            solde[i-1] += revenusDispos[i-1]
                    + revenusFonciers[i-1]
                    + gainsFinanciers[i-1]
                    - chargesFinancieres[i-1]
                    - chargesFoncieres[i-1];
            // Calcul des taxes
            if (i > 1 && i%12 == 0) {
                double gainsImposables = 0.0;
                double gainsEpargneImposables = 0.0;
                double netFoncier = 0.0;
                for (int j = 1; j <= 12; j++) {
                    gainsImposables += gainsFinanciers[i-j];
                    gainsEpargneImposables += gainsFinanciersEpargne[i-j];
                    netFoncier += revenusFonciers[i-j] - chargesFoncieres[i-j] - chargesFinancieresLocatives[i-j];
                }
                System.out.println("net foncier avant report = " + netFoncier);
                if (netFoncier > 0) {
                    if (reportDeficitFoncier > 0) {
                        double report = Math.min(reportDeficitFoncier, netFoncier);
                        reportDeficitFoncier -= report;
                        netFoncier -= report;
                    }
                    gainsImposables += netFoncier;
                }
                System.out.println("net foncier = " + netFoncier);
                solde[i-1] -= gainsImposables
                        * (tauxCSG + tauxMarginal) / 100.0;
                epargne[i-1] -= gainsEpargneImposables * tauxCSG / 100.0;
              }
            if (solde[i-1] < 0) {
                System.err.println("Déficit sur la période " + i
                    + ": " + solde[i-1]);
            }
        }
    }

    public double getTotalAllocation() {
        double sum = 0.0;
        for (List<CashFlow> cashFlows: m_cashFlows) {
            for (CashFlow cf: cashFlows) {
                if (cf.m_allocation) {
                    sum += cf.m_montant;
                }
            }
        }
        return sum;
    }

    public double getTotalChargesFinancieres() {
        double sum = 0.0;
        for (List<CashFlow> cashFlows: m_cashFlows) {
            for (CashFlow cf: cashFlows) {
                if (cf.m_chargeFinanciere) {
                    sum += cf.m_montant;
                }
            }
        }
        return -sum;
    }

    public double getTotalChargesFinancieresFoncieres() {
        double sum = 0.0;
        for (List<CashFlow> cashFlows: m_cashFlows) {
            for (CashFlow cf: cashFlows) {
                if (cf.m_chargeFinanciere
                        && cf.m_chargeFonciere) {
                    sum += cf.m_montant;
                }
            }
        }
        return -sum;
    }

    private double getTotal(Predicate predicate) {
        double sum = 0.0;
        for (List<CashFlow> cashFlows: m_cashFlows) {
            for (CashFlow cf: cashFlows) {
                if (predicate == null || predicate.matches(cf)) {
                    sum += cf.m_montant;
                }
            }
        }
        return sum;
    }

    private double getNAV(int i) {
        double nav = getTotal(null);
        for (Contrat c: m_contrats) {
            nav += c.getNAV(i);
        }
        return nav;
    }

    public double getFutureValue(int from, int to, double rate) {
        double res = 0.0;
        for (int i = from; i <= to && i <= duree; i++) {
            for (CashFlow cf: m_cashFlows[i-1]) {
                if (cf.m_allocation) {
                    res += cf.m_montant * Math.pow(1.0 + rate, (to-from)/12.0);
                }
            }
        }
        return res;
    }

    public double getIRR(int from, int to, double nav) {
        double rateLo = 0.01;
        double rateHi = 1.0;
        double navLo = getFutureValue(from, to, rateLo);
        double navHi = getFutureValue(from, to, rateHi);
        if (nav < navLo || nav > navHi) {
            throw new RuntimeException("IRR out of range");
        }
        while (Math.abs(navHi-navLo) > 1.0) {
            double newRate = 0.5 * (rateLo+rateHi);
            double newNav = getFutureValue(from, to, newRate);
            if (newNav < nav) {
                rateLo = newRate;
                navLo = newNav;
            } else {
                rateHi = newRate;
                navHi = newNav;
            }
        }
        return 0.5*(rateLo+rateHi);
    }

    public String toString() {
        String s = "";
        s += "Durée de la simulation: " + duree + " mois ("
        + duree/12.0 + " ans).\n";
        s += "Solde final: " + solde[duree-1] + "\n";
        double d = 0.0;
        for (int i = 0; i < duree; i++) {
            d+= gainsFinanciers[i];
        }
        s += "Total gains financiers: " + d + "\n";
        d = solde[duree-1] + epargne[duree-1];
        for (Achat a: achats) {
            d += a.appart.getValo();
        }
        s += "Total actifs: " + d + "\n";

        // Cash flows
        s += "Total alloué au projet: " + getTotalAllocation() + "\n";

        s += "Total charges financières: " + getTotalChargesFinancieres() + "\n";
        s += "dont charges financières foncières: " + getTotalChargesFinancieresFoncieres() + "\n";
        s += "Total des charges foncières: " + getTotal(CashFlow.PRED_CHARGES_FONCIERES) + "\n";
        s += "dont non récupérables: " + getTotal(CashFlow.PRED_CHARGES_NON_RECUPERABLES) + "\n";
        s += "Total des impôts: " + getTotal(CashFlow.PRED_IMPOTS) + "\n";
        s += "Total des revenus fonciers: " + getTotal(CashFlow.PRED_REVENUS_FONCIERS) + "\n";
        s += "Total des cash flows: " + getTotal(null) + "\n";
        s += "NAV finale: " + getNAV(duree + 1) + "\n";
        s += "IRR final: " + getIRR(1, duree+1, getNAV(duree+1)) + "\n";
        return s;
    }

    /**
     * Ajoute un contrat à la simulation.
     */
    public void addContrat(Contrat contrat) {
        m_contrats.add(contrat);
    }

    /**
     * Ajoute une stratégie à la simulation.
     */
    public void addStrategie(Strategie strategie) {
        m_strategies.add(strategie);
    }
}
