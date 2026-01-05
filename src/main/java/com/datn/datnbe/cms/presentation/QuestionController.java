package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.entity.Question;
import com.datn.datnbe.cms.entity.questiondata.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @PostMapping("/batch")
    public ResponseEntity<?> saveQuestionBatch(@RequestBody List<Question> questions) {
        System.out.println("Received " + questions.size() + " questions");
        
        for (Question q : questions) {
            System.out.println("Question ID: " + q.getId());
            System.out.println("Type: " + q.getType());
            System.out.println("Title: " + q.getTitle());
            System.out.println("Data class: " + q.getData().getClass().getSimpleName());
            
            if (q.getData() instanceof MultipleChoiceData) {
                MultipleChoiceData mcd = (MultipleChoiceData) q.getData();
                System.out.println("Options count: " + mcd.getOptions().size());
            } else if (q.getData() instanceof MatchingData) {
                MatchingData md = (MatchingData) q.getData();
                System.out.println("Pairs count: " + md.getPairs().size());
            } else if (q.getData() instanceof OpenEndedData) {
                System.out.println("Open-ended question");
            } else if (q.getData() instanceof FillInBlankData) {
                FillInBlankData fibd = (FillInBlankData) q.getData();
                System.out.println("Segments count: " + fibd.getSegments().size());
            }
            System.out.println("---");
        }
        
        return ResponseEntity.ok("Saved " + questions.size() + " questions");
    }

    @PostMapping("/single")
    public ResponseEntity<?> saveSingleQuestion(@RequestBody Question question) {
        System.out.println("Received single question");
        System.out.println("Type: " + question.getType());
        System.out.println("Data class: " + question.getData().getClass().getSimpleName());
        
        return ResponseEntity.ok("Saved question of type: " + question.getType());
    }
}

