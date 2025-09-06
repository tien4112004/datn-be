package com.datn.datnbe.document.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.datn.datnbe.document.enums.SlideElementType;
import org.springframework.stereotype.Component;

@EnableMongoRepositories(basePackages = "com.datn.datnbe.document.repository")
@Component
public class DocumentMongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory,
            MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
    }

    // @Bean(name = "mongoTransactionManager")
    // @ConditionalOnProperty(
    // prefix = "spring.data.mongodb.transactions",
    // name = "enabled",
    // havingValue = "true",
    // matchIfMissing = true
    // )
    // public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory
    // mongoDatabaseFactory) {
    // return new MongoTransactionManager(mongoDatabaseFactory);
    // }
    //
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new SlideElementTypeReadConverter());
        converters.add(new SlideElementTypeWriteConverter());
        return new MongoCustomConversions(converters);
    }

    public static class SlideElementTypeReadConverter implements Converter<String, SlideElementType> {
        @Override
        public SlideElementType convert(String source) {
            return SlideElementType.fromValue(source);
        }
    }

    public static class SlideElementTypeWriteConverter implements Converter<SlideElementType, String> {
        @Override
        public String convert(SlideElementType source) {
            return source.getValue();
        }
    }
}
