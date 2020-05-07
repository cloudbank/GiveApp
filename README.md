# Give v1

  <img src="https://i.imgur.com/ZLvIgPs.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://i.imgur.com/xFMatmK.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://i.imgur.com/X05HI4M.png" height="350"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;


###   --A Charity API app for helping reach those in need

Androidx, ICS(15) to Android10, latest best practices @ google

`MVVM` pattern with `Paging`, `LiveData` and `Room`, and `Repository` pattern are used to page in data
for the UI and also back-fill from the network as the user reaches the end of the list or LiveData detects a change.
Swipe to refresh is available on the toolbar to get the very latest.

`Room` uses a `DataSource.Factory` as a positional data source and the Paging Boundary Callback
API to get notified when the Paging library consumes the available local data.  NetworkState implementation
keeps track of network status.

Cached content is always available on the device and the user will still have a good experience even if the network is slow /
unavailable ---> OFFLINE_MODE!
Glide caches images as long as they are initially loaded.


### Libraries
* [Androidx][]
* [Android Architecture Components][arch]
* [Retrofit][retrofit] for REST api communication
* [Glide][glide] for image loading
* [espresso][espresso] for UI tests
* [mockito][mockito] for mocking in tests
* [Retrofit Mock][retrofit-mock] for creating a fake API implementation for tests

[mockwebserver]: https://github.com/square/okhttp/tree/master/mockwebserver
[support-lib]: https://developer.android.com/topic/libraries/support-library/index.html
[arch]: https://developer.android.com/arch
[espresso]: https://google.github.io/android-testing-support-library/docs/espresso/
[retrofit]: http://square.github.io/retrofit
[glide]: https://github.com/bumptech/glide
[mockito]: http://site.mockito.org
[retrofit-mock]: https://github.com/square/retrofit/tree/master/retrofit-mock
