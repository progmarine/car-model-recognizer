package face.recognition.ai.controller

import ai.djl.modality.Classifications
import face.recognition.ai.common.CustomResponse
import face.recognition.ai.service.ImageComparator
import face.recognition.ai.service.predict.Predict
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.view.RedirectView

@Api(tags = ["Image Controller"])
@RestController(value = "api/v1")
class ImageController(
    private val predictService: Predict,
    private val imageComparator: ImageComparator
) {

    @GetMapping("/")
    fun rootToSwaggerUiRedirect(): RedirectView {
        return RedirectView("/swagger-ui/")
    }

    @ApiOperation(
        value = "Classify car model by car picture with MLP algorithm",
        notes = "Attach image of any car before 2012 y."
    )
    @RequestMapping(method = [RequestMethod.POST], value = ["/predict"])
    fun predictImageClass(@RequestParam("file") multipartFile: MultipartFile): CustomResponse<Classifications> {
        val result = predictService.predict(multipartFile)
        return CustomResponse(
            status = HttpStatus.OK.value(),
            action = CustomResponse.CustomReponseAction.SUCCESSFUL,
            data = result
        )
    }

    @ApiOperation(
        value = "Compare two images",
        notes = "Attach two images of same shape (will be adjusted if not same)"
    )
    @RequestMapping(method = [RequestMethod.POST], value = ["/compare"])
    fun compareImages(
        @RequestParam("file") multipartFile1: MultipartFile,
        @RequestParam("file") multipartFile2: MultipartFile
    ): CustomResponse<String> {
        val result = imageComparator.getDiff(multipartFile1, multipartFile2)
        return CustomResponse(
            status = HttpStatus.OK.value(),
            action = CustomResponse.CustomReponseAction.SUCCESSFUL,
            data = result
        )
    }

}