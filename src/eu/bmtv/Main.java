package eu.bmtv;

public class Main {

    public static void main(String[] args) {
        Simulation simu = new Simulation();
        //simu.addContrat(new Contrat.RevenusMisADisposition(500));
        simu.addStrategie(new Strategie.CombleDeficit(1000.0));
        simu.revenusNonFonciersDispos = 1000;
        simu.tauxFinancier = 1.5;
        simu.tauxEpargne = 3.0;
        Bonus b = new Bonus(1, 175000.0);
        //simu.addContrat(b);
        //simu.boni.add(b);
	    Emprunt e = new Emprunt(528000, 1, 18*12, 2.8, false);
        //simu.addContrat(e);
        Dette dette = new Dette(e, 1, false);
        //simu.dettes.add(dette);
        e = new Emprunt(160000, 1, 18*12, 3.4, true);
        simu.addContrat(e);
        dette = new Dette(e, 1, true);
        simu.dettes.add(dette);
        BienImmobilier appart = new BienImmobilier();
        appart.chargesRecuperables = 0*12;
        appart.chargesNonRecuperables = 100*12;
        appart.loyerHc = 14000;
        appart.prixAchat = 160000;
        appart.prixTravauxReno = 000;
        appart.surface = 65;
        appart.taxeFonciere = 800;//878;
        simu.addContrat(appart);
        Achat achat = new Achat();
        achat.appart = appart;
        achat.dateAchat = 1;
        simu.achats.add(achat);
        simu.duree = 12*18;
        long t0 = System.currentTimeMillis();
        simu.calcule();
        //System.out.println(e.getLoyer(1) + " " + e.getLoyer(e.duree) + " (" + (System.currentTimeMillis() - t0) + ")");
        //System.out.println("Coût: " + e.getTotalInterets());
        System.out.println(simu);
                                                   /*
        System.out.println(Math.floor(Math.random() * 6));
        String[] names = new String[] {"Antoine", "Aurélie", "Bénédicte",
                "Emmanuel", "François","Marie-Jeanne", "Matthieu"};
        int[] so = new int[] {5, 4, 6, 3, 1, 0, 2};
        int[] peer = new int[names.length];
        Set<Integer> allocated = new TreeSet<Integer>();
        for (int i = 0; i < names.length; i++) {
            int alea = (int) Math.floor(Math.random() * names.length);
            while (allocated.contains(alea) || alea == i || alea == so[i]) {
                alea = (alea + 1) % names.length;
            }
            allocated.add(alea);
            peer[i] = alea;
        }
        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i] + " offre à " + names[peer[i]]);
        }
        */
    }
}
