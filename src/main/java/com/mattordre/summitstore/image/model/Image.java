package com.mattordre.summitstore.image.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "image")
@Table(name = "image")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Image {

    @Id
    @Column(name = "file_name", nullable = false, unique = true, length = 100)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, insertable = false, length = 20)
    private ImageType type;

    @Column(name = "bucket_name", nullable = false, length = 50)
    private String bucketName;

}
