package uk.ac.man.cs.eventlite.dao;


import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count(){
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAll();
	}

	public Event save(Event e) {
		return eventRepository.save(e);
	}

	@Override
	public Iterable<Event> findAllByOrderByDateAscNameAsc() {
		return eventRepository.findAllByOrderByDateAscNameAsc();
	}


	@Override
	public Optional<Event> findById(long id) {
		 return eventRepository.findById(id);
		 
	}
	
	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}

	@Override
	public void deleteById(long id)
	{
		eventRepository.deleteById(id);

	}

	@Override
	public Iterable<Event> findAllByVenueOrderByDateAscNameAsc(Venue venue) {
		// TODO Auto-generated method stub
		return eventRepository.findAllByVenueOrderByDateAscNameAsc(venue);
	}

	
	public Iterable<Event> findByNameContainingIgnoreCaseOrderByDateAscNameAsc(String name){
		return eventRepository.findByNameContainingIgnoreCaseOrderByDateAscNameAsc(name);
	}

	@Override
	public Event findOne(long id) {		
		return eventRepository.findById(id).orElse(null);
	}
}
