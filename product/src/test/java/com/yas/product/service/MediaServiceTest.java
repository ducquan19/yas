package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.product.viewmodel.NoFileMediaVm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ServiceUrlConfig serviceUrlConfig;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void getMedia_whenIdIsNull_thenReturnEmptyNoFileMediaVmAndSkipHttpCall() {
        NoFileMediaVm result = mediaService.getMedia(null);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNull();
        assertThat(result.url()).isEmpty();
        verify(restClient, never()).get();
    }
}
