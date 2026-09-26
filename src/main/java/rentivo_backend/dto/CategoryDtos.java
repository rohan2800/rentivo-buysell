package rentivo_backend.dto;
import rentivo_backend.entity.CategoryField.FieldType;
import java.util.*;
public final class CategoryDtos { private CategoryDtos(){}
 public record FieldRequest(String name,FieldType type,boolean required,String optionsCsv,Integer sortOrder){}
 public record CategoryRequest(String name,boolean active,List<FieldRequest> fields){}
}
