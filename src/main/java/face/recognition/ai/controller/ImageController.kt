package face.recognition.ai.controller

import ai.djl.modality.Classifications
import face.recognition.ai.common.CustomResponse
import face.recognition.ai.service.ImageComparator
import face.recognition.ai.service.predict.Predict
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController(value = "api/v1")
class ImageController(
    private val predictService: Predict, private val imageComparator: ImageComparator
) {

    @RequestMapping(method = [RequestMethod.POST], value = ["/predict"])
    fun predictClass(multipartFile: MultipartFile): CustomResponse<Classifications> {
        val result = predictService.predict(multipartFile)
        return CustomResponse(
            status = HttpStatus.OK.value(),
            action = CustomResponse.CustomReponseAction.SUCCESSFUL,
            data = result
        )
    }

    @RequestMapping(method = [RequestMethod.POST], value = ["/compare"])
    fun predictClass(
        multipartFile1: MultipartFile,
        multipartFile2: MultipartFile
    ): CustomResponse<String> {
        val result = imageComparator.getDiff(multipartFile1, multipartFile2)
        return CustomResponse(
            status = HttpStatus.OK.value(),
            action = CustomResponse.CustomReponseAction.SUCCESSFUL,
            data = result
        )
    }
}