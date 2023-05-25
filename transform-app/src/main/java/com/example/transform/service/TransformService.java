package com.example.transform.service;

import com.example.transform.domain.PricingElem;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.OVERWRITE;
import static org.apache.parquet.hadoop.metadata.CompressionCodecName.SNAPPY;


/**
 * The TransformService class provides methods for parsing and transforming data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransformService {
    private final JsonFactory jsonFactory;
    private final SchemaService schemaService;

    /**
     * Parses the input stream and invokes the provided consumer for each pricing element found.
     *
     * @param is The input stream to parse.
     * @param consumer The consumer to invoke for each pricing element.
     * @return The TransformationResult object containing the schema, parse count, and file.
     * @throws IOException If an I/O error occurs during parsing.
     */
    public TransformationResult parse(InputStream is, BiConsumer<HashMap<String, Class<?>>, PricingElem> consumer) throws IOException {
        var schema = new HashMap<String, Class<?>>();
        JsonParser jsonParser = jsonFactory.createParser(is);
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            if ("Response".equals(fieldName)) {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    String responseFieldName = jsonParser.getCurrentName();
                    if ("pricingLineList".equals(responseFieldName)) {
                        processPricingLineList(jsonParser, schema, consumer);
                    }
                }
            } else if ("pricingLineList".equals(fieldName)) {
                processPricingLineList(jsonParser, schema, consumer);
            }
        }
        jsonParser.close();
        return new TransformationResult(schema, 0, null);
    }

    /**
     * Parses the input stream and transforms the data into a parquet file.
     *
     * @param is The input stream to parse.
     * @return The TransformationResult object containing the schema, parse count, and file.
     * @throws IOException If an I/O error occurs during parsing or writing to the parquet file.
     */
    public TransformationResult parse(InputStream is, CompressionCodecName codec) throws IOException {
        File file = File.createTempFile("req-" + UUID.randomUUID(), ".avro");
        final AtomicBoolean forTheFirstTime = new AtomicBoolean();
        final AtomicLong counter = new AtomicLong(0);
        var schemaRef = new AtomicReference<Schema>();
        var writerRef = new AtomicReference<ParquetWriter<GenericRecord>>();
        var result = parse(is, (cols, object) -> {
            try {
                if (counter.incrementAndGet() % 6144 == 0) {
                    log.info("{}", counter.get());
                }
                if (!forTheFirstTime.get()) {
                    var textSchema = schemaService.generateSchema(cols);
                    var schema = new Schema.Parser().parse(textSchema);
                    schemaRef.set(schema);
                    Configuration configuration = new Configuration();
                    Path path = new Path(file.getAbsolutePath());

                    ParquetWriter<GenericRecord> writer = AvroParquetWriter
                            .<GenericRecord>builder(path)
                            .withWriteMode(OVERWRITE)
                            .withSchema(schema)
                            .withCompressionCodec(codec)
                            .withConf(configuration)
                            .build();
                    writerRef.set(writer);
                    forTheFirstTime.set(true);
                }

                var builder = new GenericRecordBuilder(schemaRef.get());
                object.props().forEach(builder::set);
                var genericRecord = builder.build();
                writerRef.get().write(genericRecord);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            writerRef.get().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("output = {}", file);
        return new TransformationResult(result.schema, result.parseCount, file);
    }


    /**
     * Processes the pricing line list from the JSON parser and invokes the provided consumer for each pricing element.
     *
     * @param jsonParser The JSON parser.
     * @param schema The schema map to update with field names and types.
     * @param consumer The consumer to invoke for each pricing element.
     * @throws IOException If an I/O error occurs during parsing.
     */
    private void processPricingLineList(JsonParser jsonParser,
                                        HashMap<String, Class<?>> schema,
                                        BiConsumer<HashMap<String, Class<?>>, PricingElem> consumer) throws IOException {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            var result = new PricingElem();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                var token = jsonParser.currentToken();

                switch (token) {
                    case VALUE_STRING: {
                        var strName = jsonParser.getCurrentName();
                        var strValue = jsonParser.getValueAsString();
                        result.props(strName, strValue);
                        schema.put(strName, String.class);
                        break;
                    }

                    case VALUE_NUMBER_FLOAT: {
                        var floatName = jsonParser.getCurrentName();
                        var floatValue = jsonParser.getDoubleValue();
                        result.props(floatName, floatValue);
                        schema.put(floatName, Double.class);
                        break;
                    }

                    case VALUE_NUMBER_INT: {
                        var intName = jsonParser.getCurrentName();
                        var intValue = jsonParser.getLongValue();
                        result.props(intName, intValue);
                        schema.put(intName, Long.class);
                        break;
                    }

                    case VALUE_FALSE: {
                        var boolName = jsonParser.getCurrentName();
                        result.props(boolName, false);
                        schema.put(boolName, Boolean.class);
                        break;
                    }

                    case VALUE_TRUE: {
                        var boolName = jsonParser.getCurrentName();
                        result.props(boolName, true);
                        schema.put(boolName, Boolean.class);
                        break;
                    }
                }
            }
            consumer.accept(schema, result);
        }
    }


    public record TransformationResult(Map<String, Class<?>> schema, long parseCount, File file) {
    }


}
