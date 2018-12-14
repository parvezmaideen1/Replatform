package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;
import org.superbiz.moviefun.blobstore.S3Store;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;



    private   BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {

        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }
    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, getCoverFile(albumId));
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException  {

        Optional<Blob> optionalBlob = blobStore.get(format("%d", albumId));

        Blob blob = optionalBlob.orElseGet(() -> getExistingCoverPath());

        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
        HttpHeaders headers = createImageHttpHeaders(blob.contentType,imageBytes);
        return new HttpEntity<>(imageBytes,headers);

    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        Blob blob = new Blob(targetFile.getName(),uploadedFile.getInputStream(),uploadedFile.getContentType());
       /* targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }*/
        //FileStore fileStore = new FileStore();
        //fileStore.put(blob);
        blobStore.put(blob);
    }

    private HttpHeaders createImageHttpHeaders(String coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Blob getExistingCoverPath() {
        ClassLoader system = getClass().getClassLoader();
        InputStream  inputStream= system.getResourceAsStream("default-cover.jpg");
        Tika tika = new Tika();
        Blob blob = new Blob("default-cover.jpg",inputStream, MimeTypeUtils.IMAGE_JPEG_VALUE);
        return blob;
    }
}
