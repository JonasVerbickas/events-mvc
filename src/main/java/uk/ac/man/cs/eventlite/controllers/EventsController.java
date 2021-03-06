package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate; 
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Tweet;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.dao.TwitterServiceImpl;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	
	@Autowired
    private VenueService venueService;

	@Autowired
	private TwitterServiceImpl twitterService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	private void sortPastAndFutureEvents(Iterable<Event> events, Model model)
	{
		List<Event> past_events = new ArrayList<Event>();
		List<Event> future_events = new ArrayList<Event>();

		LocalDate now = LocalDate.now();
		System.out.println("NOW" + now);
		for (Event e : events) {
			if(e.getDate() == null)
			{
				past_events.add(e);
			}
			else if(e.getDate().compareTo(now) < 0)
			{
				past_events.add(e);
			}
			else{
				future_events.add(e);
			}
		}
		model.addAttribute("past_events", past_events);
		model.addAttribute("future_events", future_events);
	}

	@GetMapping
	public String getAllEvents(Model model)  {
		Iterable<Event> events = eventService.findAllByOrderByDateAscNameAsc();
		model.addAttribute("events", events);
		this.sortPastAndFutureEvents(events, model);
		List<Status> timeline = twitterService.getTimeline();
		model.addAttribute("timeline", timeline);
		return "events/index";
	}


	// `prev_tweet_text` is used to display a text of the tweet made by the current user for this particular event
	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model, @ModelAttribute("prev_tweet_text") String prev_tweet_text)  {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

		model.addAttribute("event", event);
		model.addAttribute("tweet", new Tweet());
		
		return "events/event_detail";
	}

	@GetMapping("/edit/{id}")
	public String updatePageRedirect(Model model, @PathVariable("id") long id) {
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("event_old", event);
		model.addAttribute("venues", venueService.findAll());

		return "events/edit";
	}

	@DeleteMapping("/{id}/delete")
	public String deleteById(@PathVariable("id") long id) {
		eventService.deleteById(id);
		return "redirect:/events";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search")
	public String getSearchedEvent(Model model, @RequestParam(value = "search", required = true) String search_value){
		model.addAttribute("search_value", search_value);
		// Event stuff
		Iterable<Event> events = eventService.findByNameContainingIgnoreCaseOrderByDateAscNameAsc(search_value);
		model.addAttribute("events", events);
		this.sortPastAndFutureEvents(events, model);
		// Twitter stuff
		List<Status> timeline = twitterService.getTimeline();
		model.addAttribute("timeline", timeline);
		return "events/index";
	}

	@PutMapping("/{id}/delete/time")
	public String deleteTime(@PathVariable("id") long id) {
		Event e = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		e.setTime(null);
		eventService.save(e);
		return "redirect:/events/" + id;
	}

	@PutMapping("/{id}/delete/date")
	public String deleteDate(@PathVariable("id") long id) {
		Event e = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		e.setDate(null);
		eventService.save(e);
		return "redirect:/events/" + id;
	}

	@PutMapping("/{id}/delete/all_fields")
	public String deleteAllFields(@PathVariable("id") long id) {
		Event e = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		e.setDate(null);
		e.setDescription(null);
		e.setTime(null);
		eventService.save(e);
		return "redirect:/events/" + id;
	}

	@GetMapping("/new")
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
			model.addAttribute("venue", venueService.findAll());
		}
		return "events/new";
	}


	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		model.addAttribute("venue", venueService.findAll());
		if (errors.hasErrors()) {
			model.addAttribute("event");
			return "events/new";
		}
		
			eventService.save(event);
			return "redirect:/events";

	}


	@PostMapping("/edit/{id}")
	public String updateById(Model model, @PathVariable("id") long id, @Valid @ModelAttribute("event_old") Event event_in, BindingResult errors, RedirectAttributes redirectAttrs ) {
			
		
		Event eventToEdit = eventService.findById(id).get();
	
		if (errors.hasErrors()){
			model.addAttribute("event_old", event_in);
			model.addAttribute("venues", venueService.findAll());
			return String.format("events/edit", event_in.getId());
			}	
			
		
		eventToEdit.setName(event_in.getName());
		
		
		eventToEdit.setDate(event_in.getDate());
		

		eventToEdit.setTime(event_in.getTime());

		eventToEdit.setVenue(event_in.getVenue());

		eventToEdit.setDescription(event_in.getDescription());
		
		
//		if(eventToEdit.checkAll(eventToEdit) == "All pass") {
//			model.addAttribute("error", "");
//			eventService.save(eventToEdit);
//			return "redirect:/events";
//		}else {
//			String errorMsg = eventToEdit.getErrorMsg();
//			model.addAttribute("error", errorMsg);
//			return "events/edit";
//		}

		eventService.save(eventToEdit);

		return "redirect:/events";
	}

	@PostMapping("/{id}/post_tweet")
	public ModelAndView postTweet(Model model, @PathVariable("id") long id, @ModelAttribute Tweet tweet)  {
		boolean successful = twitterService.postATweet(tweet.getTweetText());
		if (!successful)
		{
			tweet.setTweetText("ERROR");
		}
		ModelMap modelMap = new ModelMap("prev_tweet_text", tweet.getTweetText());
		return new ModelAndView("redirect:/events/" + id, modelMap);
	}
}
