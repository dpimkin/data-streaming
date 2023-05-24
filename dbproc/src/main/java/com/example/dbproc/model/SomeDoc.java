package com.example.dbproc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("some")
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SomeDoc {

    @Id
    private String id;

    private String payload;
}
