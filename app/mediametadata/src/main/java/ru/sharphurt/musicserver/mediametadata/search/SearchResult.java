package ru.sharphurt.musicserver.mediametadata.search;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult<T> {

    Long resultCount;

    List<T> results;
}
