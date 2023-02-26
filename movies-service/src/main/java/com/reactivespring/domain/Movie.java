package com.reactivespring.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@With
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private MovieInfo movieInfo;
    private List<Review> reviewList;
}
