package com.etransportation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etransportation.payload.request.LikeCarRequest;
import com.etransportation.payload.request.PagingRequest;
import com.etransportation.service.LikeService;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping
    public ResponseEntity<?> likeCar(@RequestBody LikeCarRequest likeCarRequest) {
        try {
            likeService.likeCar(likeCarRequest);
        } catch (Exception e) {
            if (e.getCause().getCause().getMessage().contains("duplicate")) {
                // return ResponseEntity.badRequest().body("You have already liked this car");
                throw new IllegalStateException("You have already liked this car");
            }

        }

        return ResponseEntity.ok("Xe đã được thêm vào danh sách yêu thích");
    }

    @DeleteMapping
    public ResponseEntity<?> cancelLikeCar(@RequestBody LikeCarRequest likeCarRequest) {
        likeService.cancelLikeCar(likeCarRequest);
        return ResponseEntity.ok("Đã xóa xe khỏi danh sách yêu thích");
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<?> findAllLikeCarByAccountId(@PathVariable Long id, PagingRequest pagingRequest) {
        return ResponseEntity.ok(likeService.findAllLikeCarByAccountId(id, pagingRequest));
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkLikeCar(@RequestBody LikeCarRequest likeCarRequest) {
        return ResponseEntity.ok(likeService.checkLikeCar(likeCarRequest));
    }

}
