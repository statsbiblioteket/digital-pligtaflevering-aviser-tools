package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.RegenerateChecksumfileMain.RegenerateChecksumfileModule.md5ForClosableInputStream;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RegenerateChecksumfileMainTest {

    @Test
    public void md5ForInputStreamSimpleTests() {
        assertThat(md5ForClosableInputStream(new ByteArrayInputStream(new byte[]{})), is("d41d8cd98f00b204e9800998ecf8427e"));
        assertThat(md5ForClosableInputStream(new ByteArrayInputStream(new byte[]{'1', '2', '3', '\n'})), is("ba1f2511fc30423bdbb183fe33f3dd0f"));
        assertThat(md5ForClosableInputStream(new ByteArrayInputStream("123\n".getBytes())), is("ba1f2511fc30423bdbb183fe33f3dd0f"));
        assertThat(md5ForClosableInputStream(new ByteArrayInputStream("The quick brown fox...\n".getBytes())), is("551876c74b1cf2a8c4e857c6edeb8c9f"));

        assertThat(md5ForClosableInputStream(new ByteArrayInputStream(new byte[]{(byte) 0xc3, (byte) 0xa6, (byte) 0xc3, (byte) 0xb8, (byte) 0xc3, (byte) 0xa5, '\n'})),
                is("7d59a3d1ae4fd15770ad1567ffc02c06"));

    }

    @Test
    public void md5ForInputStreamLargeTests() throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         // yes | head -200000 >yes ; md5sum yes
        for (long l = 0; l < 200000; l++) {
            baos.write(new byte[] {'y', '\n'});
        }
        assertThat(md5ForClosableInputStream(new ByteArrayInputStream(baos.toByteArray())), is("c2938b130a1d2db9597a9c9a8ea2a5cf"));
    }
}
