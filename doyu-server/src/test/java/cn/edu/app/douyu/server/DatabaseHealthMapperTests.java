package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.persistence.DatabaseHealthMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseHealthMapperTests {
    @Autowired
    DatabaseHealthMapper mapper;

    @Test
    void mybatisMapperCanQueryDatabase() {
        assertThat(mapper.ping()).isEqualTo(1);
    }
}
