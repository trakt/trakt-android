# OpenApi Changes TODO List

### [GetSyncProgressUpNextStandard200ResponseInnerProgress.kt](../build/generate-resources/main/src/main/kotlin/org/openapitools/client/models/GetSyncProgressUpNextStandard200ResponseInnerProgress.kt) 
 - nullable should be next episode in case of intent "completed"

### Up Next
- missing sort_by and sort_how params
```
https://apiz.trakt.tv/sync/progress/up_next_nitro?page=1&limit=10&intent=continue&sort_by=remaining&sort_how=asc
```

### PutUsersCoverRequest
- val coverId: java.math.BigDecimal to Int

### PostScrobbleEpisodeStartRequest
- progress Int -> Float

### GetShowsEpisodeStats200Response
- missing prop `favorited`

### [ScrobbleExtrasApi.kt](../common/src/main/java/tv/trakt/trakt/common/networking/api/scrobble/ScrobbleExtrasApi.kt)
- all methods should be in OpenApi
- DeleteUsersFiltersDeleteIdParameter is an empty class ID 

### GetUsersHistoryAll200ResponseInner
- fix polymorphic response

