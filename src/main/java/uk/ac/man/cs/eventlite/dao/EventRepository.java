package uk.ac.man.cs.eventlite.dao;


import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long>  {
	public Iterable<Event> findAllByOrderByDateAscNameAsc();

	public Iterable<Event> findAll();

	public Optional<Event> findById(long id);

	public void deleteById(long id);
	
	public Iterable<Event> findByNameContainingIgnoreCaseOrderByDateAscNameAsc(String name);
	
	public Iterable<Event> findAllByVenueOrderByDateAscNameAsc(Venue venue);
	
}
