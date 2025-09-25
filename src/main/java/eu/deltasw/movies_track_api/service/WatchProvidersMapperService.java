package eu.deltasw.movies_track_api.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import eu.deltasw.movies_track_api.model.entity.ProviderInfo;
import info.movito.themoviedbapi.model.core.watchproviders.Provider;
import info.movito.themoviedbapi.model.core.watchproviders.WatchProviders;

@Service
public class WatchProvidersMapperService {

    private final ModelMapper modelMapper;

    public WatchProvidersMapperService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProviderInfo convertTo(Provider provider) {
        return modelMapper.map(provider, ProviderInfo.class);
    }

    public Set<ProviderInfo> convertTo(WatchProviders watchProviders) {
        if (watchProviders == null) {
            return null;
        }
        Set<ProviderInfo> dto = new HashSet<>();

        List<ProviderInfo> rentProviders = mapProviderList(watchProviders.getRentProviders());
        if (rentProviders != null) {
            dto.addAll(rentProviders);
        }

        List<ProviderInfo> flatrateProviders = mapProviderList(watchProviders.getFlatrateProviders());
        if (flatrateProviders != null) {
            dto.addAll(flatrateProviders);
        }

        if (flatrateProviders == null && rentProviders == null) {
            return null;
        }

        return dto;
    }

    private List<ProviderInfo> mapProviderList(List<Provider> providers) {
        return providers == null ? null
                : providers.stream()
                        .map(this::convertTo)
                        .collect(Collectors.toList());
    }
}

