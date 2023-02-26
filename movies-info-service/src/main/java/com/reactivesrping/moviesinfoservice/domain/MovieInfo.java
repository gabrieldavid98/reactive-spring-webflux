package com.reactivesrping.moviesinfoservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@With
@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class MovieInfo {
    @Id
    private String movieInfoId;

    @NotBlank
    private String name;

    @Positive
    private int year;

    @NotEmpty
    private List<@NotBlank String> cast;

    private LocalDate releaseDate;
}
