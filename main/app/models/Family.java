package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Family of a Song. It's a tree of melody generations.
 */
@Entity
public class Family extends Model {
	public String name;
	
	@ManyToOne
	public Melody root;
	
	public Integer melodyMinVoteToFilter = 10;
	
	public Integer depth = 0;
	
	public boolean finished = false;
	
	public Date created = new Date();
	
	// Properties

	public Double luckOfMutation = 0.05;
	public Double percentOfAllPossibilities = 0.6;
	public Double luckOfJustIntonation = 0.7;
	
	public Double luckToEscape1of2 = 0.9;
	public Double luckToEscape1of4 = 0.1;
	public Double luckToAvoidSamePosition = 0.1;
	
	public Integer nbMelodiesAtBootstrap = 0; // inited on family creation
	
	public Family(String name) {
		this.name = name;
	}
	
	public Family bootstrap(Integer nbMelodiesToBootstrap) {
		return bootstrap(nbMelodiesToBootstrap, 16, 20);
	}
	
	public Family bootstrap(Integer nbMelodiesToBootstrap, Integer loopLength, Integer notesLength) {
		this.nbMelodiesAtBootstrap = nbMelodiesToBootstrap;
		Melody m = new Melody(loopLength, notesLength).save();
		for(int i=0; i<nbMelodiesToBootstrap; ++i) {
		    Integer pitch = Note.randomIntonation(m.notesLength);
		    Note n = new Note(pitch, i*2).save();
		    m.notes.add(n);
		}
		m.save();
		save();
		setRoot(m);
		m.createChildrens();
		return this;
	}
	
	public Family setRoot(Melody m) {
		m.setFamily(this).save();
		root = m;
		return this;
	}
	
	/**
	 * Depth is max generation of all melodies of the family
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * Total vote to pass next depth for a generation
	 */
	public long getVoteRequiredForGeneration(Integer generation) {
		return melodyMinVoteToFilter*Melody.count("byFamilyByGeneration", this, generation);
	}
	
	/**
	 * Total vote elapsed for a generation
	 */
	public int getVoteElapsedForGeneration(int generation) {
		//return melodyMinVoteToFilter*Melody.count("byFamilyByGeneration", this, generation);
		// ("select sum(total) from Melody where family=?1 and generation=?2", this, generation)
		return 0; //todo
	}

	public Family setFinished() {
		finished = true;
		return this;
	}
	
	public static List<Family> list() {
		return find("order by date desc").fetch();
	}
}
