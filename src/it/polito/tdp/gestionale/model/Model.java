package it.polito.tdp.gestionale.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import it.polito.tdp.gestionale.db.DidatticaDAO;

public class Model {

	private List<Corso> corsi;
	private List<Studente> studenti;
	private DidatticaDAO didatticaDAO;

	private SimpleGraph<Nodo, DefaultEdge> grafo;
	private Map<Integer, Studente> mappaStudenti;
	
	public Model() {
		didatticaDAO = new DidatticaDAO();
		grafo = new SimpleGraph<Nodo, DefaultEdge>(DefaultEdge.class);
		mappaStudenti = new HashMap<Integer, Studente>();
	}
	
	public List<Studente> getTuttiStudenti(){
		if(studenti == null){
			studenti = didatticaDAO.getTuttiStudenti();
			for(Studente s : studenti){
				mappaStudenti.put(s.getMatricola(), s);
			}
		}
		return studenti;
	}
	public List<Corso> getTuttiCorsi(){
		if(corsi == null){
			corsi = didatticaDAO.getTuttiICorsi();
			getTuttiStudenti();
			for(Corso c : corsi){
				didatticaDAO.setStudentiIscrittiAlCorso(c, mappaStudenti);
				// questo metodo non torna una lista ma la imposta direttamente
			}
		}
		
		return corsi;
		
	}
	
	public void generaGrafo(){
		
		studenti = this.getTuttiStudenti();
		corsi = this.getTuttiCorsi();
		
		
		//PER FUNZIONARE SUI NODI DEVE ESSERE DEFINITO HASHCODE ED EQUALS
		Graphs.addAllVertices(grafo, studenti);
		Graphs.addAllVertices(grafo, corsi);
		
		System.out.println("\nNumero di vertici: "+grafo.vertexSet().size());
		
		//ARCHI
		for(Corso c : corsi){
			for(Studente s : c.getStudenti()){
				grafo.addEdge(c, s);
			}
		}
		System.out.println("\nNumero archi grafo: "+grafo.edgeSet().size());
		System.out.println("\nGRAFO CREATO!");
		
		
	}
	public List<Integer> getStatCorsi(){
		List<Integer> statCorsi = new ArrayList<Integer>();
		
		//iniziallizzo tutti i corsi a 0 studenti
		for(int i =0; i<corsi.size()+1; i++){
			statCorsi.add(0);
		}
		
		//aggiorno le statistiche
		for(Studente s : studenti){
			int nCorsi = Graphs.neighborListOf(grafo, s).size();
			int counter = statCorsi.get(nCorsi);
			counter++;
			statCorsi.set(nCorsi, counter);
		}
			
		return statCorsi;
	}
	

	public List<Corso> findMinimalSet(){
		
		List<Corso> parziale = new ArrayList<Corso>();
		List<Corso> migliore = new ArrayList<Corso>();
		
		//non � necessario usare il PASSO perch� l'algoritmo ricorsivo termina quando ha finito di generare tutte le
		//possibili combinazioni
		recursive(parziale, migliore);
		
		return migliore;
	}

	public void recursive(List<Corso> parziale, List<Corso> migliore) {
		
		//System.out.println(parziale);
		
		HashSet<Studente> hashSetStudenti = new HashSet<Studente>(this.getTuttiStudenti());
		for(Corso corso : parziale){
			hashSetStudenti.removeAll(corso.getStudenti());
		}
		if(hashSetStudenti.isEmpty()){
			//confronto con la migliore
			if(migliore.isEmpty()){
				migliore.addAll(parziale);
			}
			if(parziale.size()<migliore.size()){
				migliore.clear();
				migliore.addAll(parziale);
			}
		}
		
		
		//creo una soluzione parziale
		for(Corso corso : this.getTuttiCorsi()){
			//CONDIZIONI PER DIRE QUANDO POSSO AGGIUNGERE UN CORSO
			//il secondo controllo verifica l'id dei corsi, solo se � successivo si pu� inserire
			if(parziale.isEmpty()  ||  corso.compareTo(parziale.get(parziale.size()-1))>0){
				parziale.add(corso);
				
				recursive(parziale, migliore);
				
				parziale.remove(corso);
			}
		}
		
	}
	
	
}
