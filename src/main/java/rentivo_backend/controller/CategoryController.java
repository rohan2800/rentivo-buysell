package rentivo_backend.controller;
import org.springframework.web.bind.annotation.*; import rentivo_backend.entity.Category; import rentivo_backend.service.CategoryService;
@RestController @RequestMapping("/api/categories") public class CategoryController {private final CategoryService s; public CategoryController(CategoryService s){this.s=s;} @GetMapping public Object all(){return s.active();} @GetMapping("/{id}") public Category get(@PathVariable Long id){return s.get(id);} }
