package eu.gaiax.sdcreationwizard.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.startsWith;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = VicShapeProcessorApplication.class)
public class ConversionServiceBadRequestTests {

    private static final String CONVERT_FILE_URL = "/convertFile";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
        Mockito.mock(ConversionController.class);
    }

    public static Stream<String> filenames() {
        return Stream.of(
                // Test invalid sh:maxcount value (wrong datatype for eg: string) in SHACL file
                "test-invalid-max-count.ttl"
        );
    }

    /**
     * tests for cases that return 400 Bad request
     * shapes
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("filenames")
    public void testBadRequest(String inputFilename) throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(inputFilename);

        ResultMatcher badRequest = MockMvcResultMatchers.status().isBadRequest();
        ResultMatcher errorMessageBody = MockMvcResultMatchers.content().string(startsWith("Error: "));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", inputFilename,
                MediaType.MULTIPART_FORM_DATA_VALUE, is);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(CONVERT_FILE_URL)
                .file(mockMultipartFile);

        mockMvc.perform(builder)
                .andExpect(badRequest)
                .andExpect(errorMessageBody)
                .andDo(MockMvcResultHandlers.print());
    }
}
