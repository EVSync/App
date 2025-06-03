package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tqs.evsync.backend.model.Rating;
import tqs.evsync.backend.repository.RatingRepository;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepo;

    @PostMapping("/submit")
    public Rating submitRating(@RequestBody Rating rating) {
        return ratingRepo.save(rating);
    }

    @GetMapping("/station/{stationId}")
    public List<Rating> getRatings(@PathVariable Long stationId) {
        return ratingRepo.findByStationId(stationId);
    }
}
