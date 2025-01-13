package com.mattordre.summitstore.brand.model;

import com.mattordre.summitstore.image.model.BrandLogo;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "brand")
@Table(name = "brand")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @OneToOne
    @JoinColumn(name = "brand_logo_id", nullable = false)
    private BrandLogo logo;

}
