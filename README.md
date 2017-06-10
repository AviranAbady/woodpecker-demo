# Demo Android project for woodpacker

<a href="https://github.com/AviranAbady/woodpecker">woodpecker</a> is an experimental lean network manager<br/>

### Integrate
```gradle
compile 'org.aviran.woodpecker:woodpecker:0.9.1'
```

<img src="http://i.imgur.com/35jFhoU.gif"/>


### Easily perform HTTP api calls, chain api calls easily.
```java
// Initialize Woodpecker
Woodpecker.initialize(new WoodpeckerSettings("http://woodpecker.aviran.org"));

// Run the following 6 requests, consecutively, passing data from one to the other.

// POST  login    /login - post body: username=user&password=password
// GET   list     /list?page=1&pageSize=10
// GET   item     /item/{id}
// POST  review   /review - post body: { name: Aviran, review: This is awesome }
// GET   get      /image.png - download binary file
// PUT   upload   /upload - upload binary image file

Woodpecker
  .begin()  // POST /login
  .request(new LoginRequest("username", "p@ssw0rd"))
  .then(new WoodpeckerResponse<LoginResponse>() {
      @Override
      public void onSuccess(LoginResponse response) {
          // Update authentication token for the follwing requests
          Woodpecker.getSettings().addHeader("token", response.getToken());
      }
  })       // GET /list?page=1&pageSize=10
  .request(new ListRequest(1, 10))
  .then(new WoodpeckerResponse<List<ItemResponse>>() {
      @Override
      public void onSuccess(List<ItemResponse> response) {
          // Get next request object
          ItemRequest itemRequest = (ItemRequest) getNextRequest();
          // Update it
          itemRequest.setId(response.get(0).getId());
      }
  })      // GET /item/{id}   - id is updated in run time by previous request
  .request(new ItemRequest(-1))
  .then(new WoodpeckerResponse<ItemResponse>() {
      @Override
      public void onSuccess(ItemResponse response) {
      }
  })      // POST /review  - JSON encoded post
  .request(new ReviewRequest(1, "Aviran", "This is awesome!"))
  .then(new WoodpeckerResponse<String>() {
      @Override
      public void onSuccess(String response) {
      }
  })      // GET /image.png - request with progress tracking
  .request(new DownloadFileRequest(progressListener))
  .then(new WoodpeckerResponse<InputStream>() {
      @Override
      public void onSuccess(InputStream response) {
      }
  })      // POST multipart data - 2 files uploaded, progress tracking
  .request(createFileUploadRequest())
  .then(new WoodpeckerResponse<UploadResponse>() {
      @Override
      public void onSuccess(UploadResponse response) {
      }
  })     // Error handler for the entire chain
  .error(new WoodpeckerError() {
      @Override
      public void onError(WoodpeckerResponse response) {
      }
  });
```

### Login api call is defined by the following request/response classes
```java
@Post("/login")
public class LoginRequest extends WoodpeckerRequest {
    @Param
    private String username;

    @Param
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

public class LoginResponse {
    private String token;

    public String getToken() {
        return token;
    }
}
```

### List api call is defined by the following request/response classes
```java
@Get("/list")
public class ListRequest extends WoodpeckerRequest {
    @Param
    private int page;

    @Param
    private int pageSize;

    public ListRequest(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }
}

public class ItemResponse {
    private int id;
    private String name;
    private int[] values;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int[] getValues() {
        return values;
    }
}

```

### Item api call - Demonstrating using url path variable
```java
@Get("/item/{id}")
public class ItemRequest extends WoodpeckerRequest {
    @Path
    private int id;

    public ItemRequest(int id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
```

### Review api call - Demonstrating posting JSON body (no parameters)
```java
@Post("/review")
public class ReviewRequest extends WoodpeckerRequest {
    private int itemId;
    private String name;
    private String text;

    public ReviewRequest(int itemId, String name, String text) {
        this.itemId = itemId;
        this.name = name;
        this.text = text;
    }
}
```