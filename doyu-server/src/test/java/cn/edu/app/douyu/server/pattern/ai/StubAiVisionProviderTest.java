package cn.edu.app.douyu.server.pattern.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StubAiVisionProviderTest {

    private final StubAiVisionProvider provider = new StubAiVisionProvider();

    @Test
    void analyzeImage_returnsReasonableDefaults() {
        ImageAnalysisResult result = provider.analyzeImage("test-key", Map.of("style", "CUTE", "difficulty", "BEGINNER"));

        assertThat(result.subject()).isEqualTo("图片主体");
        assertThat(result.subjectType()).isEqualTo("OTHER");
        assertThat(result.subjectClarity()).isEqualTo("GOOD");
        assertThat(result.backgroundComplexity()).isEqualTo("MEDIUM");
        assertThat(result.recommendedCrop()).isNotNull();
        assertThat(result.recommendedCrop().x()).isEqualTo(0.1);
        assertThat(result.recommendedCrop().y()).isEqualTo(0.1);
        assertThat(result.recommendedCrop().width()).isEqualTo(0.8);
        assertThat(result.recommendedCrop().height()).isEqualTo(0.8);
        assertThat(result.recommendedStyle()).isEqualTo("CUTE");
        assertThat(result.recommendedDifficulty()).isEqualTo("BEGINNER");
        assertThat(result.recommendedGridWidth()).isEqualTo(48);
        assertThat(result.recommendedColorLimit()).isEqualTo(24);
        assertThat(result.beadSuitabilityScore()).isEqualTo(75);
        assertThat(result.riskFlags()).isEmpty();
        assertThat(result.advice()).isNotBlank();
    }

    @Test
    void analyzeImage_usesDefaultOptionsWhenNull() {
        ImageAnalysisResult result = provider.analyzeImage("test-key", null);

        assertThat(result.recommendedStyle()).isEqualTo("RESTORE");
        assertThat(result.recommendedDifficulty()).isEqualTo("NORMAL");
    }

    @Test
    void prepareImage_returnsOriginalFileKey() {
        ImagePrepareResult result = provider.prepareImage("test-key", List.of("REMOVE_BACKGROUND"), "CUTE");

        assertThat(result.preparedFileKey()).isEqualTo("test-key");
        assertThat(result.changedSubject()).isFalse();
        assertThat(result.safetyFlags()).isEmpty();
        assertThat(result.message()).contains("Stub");
    }
}
