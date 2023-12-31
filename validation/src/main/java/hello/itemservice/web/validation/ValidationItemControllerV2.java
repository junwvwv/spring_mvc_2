package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder){
        //WebDataBinder 에 검증기를 추가하면 해당 컨트롤러에서 검증이를 자동으로 적용
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }
    //@PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, //파라미터 위치 주의
                          RedirectAttributes redirectAttributes, Model model) {

        /**
         * 만약 @ModelAttribute 에 바인딩 시 타입 오류가 발생한다면?
         * - BindingResult 가 없으면 400 오류가 발생. 컨트롤러가 호출되지 않고 오류 페이지로 이동
         * - BindingResult 가 있으면 오류 정보 FieldError 를 담아서 컨트롤러를 정상 호출
         */

        if(!StringUtils.hasText(item.getItemName())){
            //필드에 오류가 있으면 FieldError 객체를 생성해서 bindingResult 에 담아둔다
            bindingResult.addError(new FieldError(
                    "item", "itemName", "상품 이름은 필수 입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError(
                    "item", "price","가격은 1,000원 이상 1,000,000원 이하 까지 허용합니다."));
        }
        if(item.getQuantity() == null || item.getQuantity() > 9999){
            bindingResult.addError(new FieldError(
                    "item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                //특정 필드를 넘어서는 오류의 경우 ObjectError 객체를 생성해서 담으면 된다
                bindingResult.addError(new ObjectError("globalError",
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));

            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm"; //bindingResult 는 자동으로 Model 로 넘어간다
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        /**
         * objectName : 오류가 발생한 객체 이름
         * field : 오류 필드
         * rejectedValue : 사용자가 입력된 값(거절된 값)
         * bindingFailure : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값
         * codes : 메세지 코드
         * arguments : 메세지에 사용하는 인자
         * defaultMessage : 기본 오류 메세지
         */
        if(!StringUtils.hasText(item.getItemName())){
            //rejectedValue 로 오류 발생시 사용자 입력 값을 유지할 수 있다
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(),
                    false, null, null, "상품 이름은 필수 입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(),
                    false, null, null, "가격은 1,000원 이상 1,000,000원 이하 까지 허용합니다."));
        }
        if(item.getQuantity() == null || item.getQuantity() > 9999){
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(),
                    false, null, null, "수량은 최대 9,999 까지 허용합니다."));
        }

        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                bindingResult.addError(new ObjectError("globalError", null, null,
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));

            }
        }

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        if(!StringUtils.hasText(item.getItemName())){
            /**
             * codes : 메세지 코드는 하나가 아니라 배열로 여러 값을 전달 할 수 있고 순서대로 매칭된다
             * arguments : 메세지 코드에 치환할 값을 전달한다
             */
            bindingResult.addError(new FieldError(
                    "item", "itemName", item.getItemName(),
                    false, new String[]{"required.item.itemName"}, null, null));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError(
                    "item", "price", item.getPrice(),
                    false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }
        if(item.getQuantity() == null || item.getQuantity() > 9999){
            bindingResult.addError(new FieldError(
                    "item", "quantity", item.getQuantity(),
                    false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                bindingResult.addError(new ObjectError("globalError", new String[]{"totalPriceMin"},
                        new Object[]{10000, resultPrice}, null));

            }
        }

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        //BindingResult 는 어떤 객체를 대상으로 검증하는지 이미 target 을 알고있다
        log.info("objectName = {}", bindingResult.getObjectName());
        log.info("target = {}", bindingResult.getTarget());

        /**
         * rejectValue()
         * - field : 오류 필드명
         * - errorCode : 오류 코드(메세지에 등록된 코드가 아님)
         * - errorArgs : 오류 메세지 치환 값
         * - defaultMessage : 오류 메세지를 찾을 수 없을 때 기본 메세지
         */
        if(!StringUtils.hasText(item.getItemName())){
            /**
             * 만약 required 라는 메세지만 있으면 해당 메세지를 선택해서 사용한다
             * 그러나 required.item.itemName 과 같이 객체명과 필드명을 조합한 세밀한 메세지 코드가 있으면
             * 해당 메세지를 높은 우선순위로 사용한다
             * 마치 new String[]{"required.item.itemName, "required"} 와 같이 동작한다
             */
            bindingResult.rejectValue("itemName", "required");
        }

        //단순한 기능 제공
        //ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if(item.getQuantity() == null || item.getQuantity() > 9999){
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        itemValidator.validate(item, bindingResult); //validator 분리

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * @Validated
     * - Item 에 대해 자동으로 Validator 가 동작한다
     * - WebDataBinder 에 등록한 검증기를 찾아서 실행하는데, 이때 supports() 로 검증기를 구분한다
     */
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

