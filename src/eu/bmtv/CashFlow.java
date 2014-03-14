package eu.bmtv;

import java.util.List;

/**
 * Représentation d'un flux de cash, i.e. un montant mais également
 * une liste d'attributs.
 */
public class CashFlow {
    double m_montant;
    boolean m_allocation = false;
    boolean m_salaire = false;
    boolean m_revenuFoncier = false;
    boolean m_revenuFinancier = false;
    boolean m_chargeFonciere = false;
    boolean m_chargeFinanciere = false;
    boolean m_chargeRecuperable = false;
    boolean m_impot = false;

    public CashFlow(double montant) {
        m_montant = montant;
    }

    /**
     * Crée un cash flow non typé, en particulier ne donnant lieu
     * à aucun impôt ni réduction d'impôt.
     */
    public static CashFlow newCashFlow(double montant) {
        CashFlow cf = new CashFlow(montant);
        return cf;
    }

    /**
     * Crée un cash flow de type impot.
     */
    public static CashFlow newImpot(double montant, boolean locatif) {
        if (montant < 0.0) {
            throw new RuntimeException("Paiement négatif");
        }
        CashFlow cf = new CashFlow(-montant);
        cf.m_impot = true;
        cf.m_chargeFonciere = locatif;
        return cf;
    }

    /**
     * Crée un cash flow de type réduction d'impôt.
     */
    public static CashFlow newReducImpot(double montant) {
        if (montant < 0.0) {
            throw new RuntimeException("Réduction négative");
        }
        CashFlow cf = new CashFlow(montant);
        cf.m_impot = true;
        return cf;
    }

    /**
     * Crée un cash flow de type allocation, c'est à dire un pur virement
     * interne au sein de l'entreprise familiale.
     */
    public static CashFlow newAllocation(double montant) {
        CashFlow cf = new CashFlow(montant);
        cf.m_allocation = true;
        return cf;
    }

    /**
     * Crée un cash flow de type reboursement de capital.
     */
    public static CashFlow newRemboursementCapital(double montant) {
        if (montant < 0.0) {
            throw new RuntimeException("Remboursement négatif");
        }
        CashFlow cf = new CashFlow(-montant);
        return cf;
    }

    /**
     * Crée un cash flow de type remboursement d'intérêts.
     */
    public static CashFlow newRemboursementInterets(double montant,
                                                    boolean locatif) {
        if (montant < 0.0) {
            throw new RuntimeException("Remboursement négatif");
        }
        CashFlow cf = new CashFlow(-montant);
        cf.m_chargeFinanciere = true;
        cf.m_chargeFonciere = locatif;
        cf.m_chargeRecuperable = false;
        return cf;
    }

    /**
     * Crée un cash flow de type charge locative récupérable.
     */
    public static CashFlow newChargeFonciere(double montant,
                                             boolean recuperable) {
        if (montant < 0.0) {
            throw new RuntimeException("Paiemement négatif");
        }
        CashFlow cf = new CashFlow(-montant);
        cf.m_chargeFonciere = true;
        cf.m_chargeRecuperable = recuperable;
        return cf;
    }

    /**
     * Crée un cash flow de type récupération de charge locative récupérable.
     */
    public static CashFlow newRecupChargeFonciere(double montant) {
        if (montant < 0.0) {
            throw new RuntimeException("Récupération négative");
        }
        CashFlow cf = new CashFlow(montant);
        cf.m_chargeFonciere = true;
        cf.m_chargeRecuperable = true;
        return cf;
    }

    /**
     * Crée un cash flow de type revenu foncier.
     */
    public static CashFlow newRevenuFoncier(double montant) {
        if (montant < 0.0) {
            throw new RuntimeException("Revenu négatif");
        }
        CashFlow cf = new CashFlow(montant);
        cf.m_revenuFoncier = true;
        return cf;
    }

    public static interface Predicate {
        public boolean matches(CashFlow cashFlow);
    }

    public static Predicate PRED_CHARGES_FONCIERES = new Predicate() {
        public boolean matches(CashFlow cf) {
            return cf.m_chargeFonciere;
        }
    };

    public static Predicate PRED_CHARGES_NON_RECUPERABLES = new Predicate() {
        public boolean matches(CashFlow cf) {
            return cf.m_chargeFonciere && !cf.m_chargeRecuperable;
        }
    };

    public static Predicate PRED_CHARGES_FONCIERES_FINANCIERES = new Predicate() {
        public boolean matches(CashFlow cf) {
            return cf.m_chargeFonciere && cf.m_chargeFinanciere;
        }
    };

    public static Predicate PRED_IMPOTS = new Predicate() {
        public boolean matches(CashFlow cf) {
            return cf.m_impot;
        }
    };

    public static Predicate PRED_REVENUS_FONCIERS = new Predicate() {
        public boolean matches(CashFlow cf) {
            return cf.m_revenuFoncier;
        }
    };

    public static double sum(List<CashFlow> cashFlows, Predicate predicate) {
        double sum = 0.0;
        for (CashFlow cashFlow: cashFlows) {
            if (predicate == null || predicate.matches(cashFlow)) {
                sum += cashFlow.m_montant;
            }
        }
        return sum;
    }

    @Override
    public String toString() {
        String s;
        if (m_montant == 0.0) {
            return "Cash flow nul";
        }
        if (m_montant > 0.0) {
            s = "Encaissement ";
        } else {
            s = "Paiement ";
        }
        return s + " de " + m_montant;
    }
}
