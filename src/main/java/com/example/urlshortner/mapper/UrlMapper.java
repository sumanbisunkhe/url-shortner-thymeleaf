package com.example.urlshortner.mapper;


import com.example.urlshortner.dto.request.UrlRequest;
import com.example.urlshortner.dto.response.UrlResponse;
import com.example.urlshortner.model.ShortUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlMapper INSTANCE = Mappers.getMapper(UrlMapper.class);

    UrlResponse toResponse(ShortUrl shortUrl);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shortCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "clickCount", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    ShortUrl toEntity(UrlRequest urlRequest);

    /**
            * Method to update an existing ShortUrl entity from a UrlRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shortCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "clickCount", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromRequest(UrlRequest urlRequest, @MappingTarget ShortUrl shortUrl);
}