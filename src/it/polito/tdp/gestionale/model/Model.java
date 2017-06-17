package it.polito.tdp.gestionale.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private List<Corso> finale = new ArrayList<Corso>();
	
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
	
	public List<Corso> trovaNumeroMinimoCorsi(){
		this.generaGrafo();
		List<Integer> stud = this.getStatCorsi();
		int numeroPersoneDaRaggiungere = 0;
		for(int i = 1 ; i<stud.size(); i++){
			numeroPersoneDaRaggiungere += stud.get(i);
		}
		int step = 0;
		List<Corso> parziale = new ArrayList<Corso>();
		Set<Studente> studentiRaggiunti = new HashSet<Studente>();
		this.recursive(parziale, numeroPersoneDaRaggiungere, studentiRaggiunti, step);
		
		return finale;
	}

	private void recursive(List<Corso> parziale, int numeroPersoneDaRaggiungere, Set<Studente> studentiRaggiunti, int step) {
		
		if(studentiRaggiunti.size() == numeroPersoneDaRaggiungere){
			if(finale.size()>parziale.size()){
				finale.clear();
				finale.addAll(parziale);
				return;
			}
		}
		for(Corso c : corsi){
			if(!parziale.contains(c) && c.getStudenti().size()>200){
				parziale.add(c);
				
				System.out.println(parziale);
				
				List<Studente> studenti = c.getStudenti();
				studentiRaggiunti.addAll(studenti);
				recursive(parziale, numeroPersoneDaRaggiungere, studentiRaggiunti, step+1);
				
				parziale.remove(c);
				parziale.removeAll(studenti);
			}
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
}
