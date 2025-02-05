package io.codegen.gwt.openapigenerator;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.CodegenResponse;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.templating.mustache.CamelCaseLambda;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class GwtElementalClientGenerator extends JavaClientCodegen {

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -g flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return "gwt-elemental-client";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a gwt-elemental-client client library.";
  }

  public GwtElementalClientGenerator() {
    super();


    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "gwt-elemental-client";

    this.removeOption("library");
    this.supportedLibraries.put("elemental", "GWT Elemental");

    CliOption library = (new CliOption("library", "library template (sub-template)")).defaultValue("<default>");
    library.setEnum(this.supportedLibraries);
    this.cliOptions.add(library);

    this.library = "elemental";
    this.serializationLibrary = "elemental";

    this.additionalProperties.put("openbrace", "{");
    this.additionalProperties.put("closebrace", "}");

  }

  public void processOpts() {
    super.processOpts();

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    // the template to use and the extension for each file to write
    modelTemplateFiles.clear();
    modelTemplateFiles.put("model.mustache", ".java");


    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    // the template to use and the extension for each file to write
    apiTemplateFiles.clear();
    apiTemplateFiles.put("api.mustache", ".java");
    apiTemplateFiles.put("apiClient.mustache", "Client.java");

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    // the input template or file the destination folder, relative `outputFolder` the output file
    supportingFiles.clear();
    supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
    supportingFiles.add(new SupportingFile("module.mustache", this.sourceFolder + "/..", "module.gwt.xml"));
    supportingFiles.add(new SupportingFile("elementalClient.mustache", (this.sourceFolder + File.separator + this.apiPackage().replace('.', File.separatorChar)).replace('/', File.separatorChar), "ElementalClient.java"));
    supportingFiles.add(new SupportingFile("fetchClient.mustache", (this.sourceFolder + File.separator + this.apiPackage().replace('.', File.separatorChar)).replace('/', File.separatorChar), "FetchClient.java"));
    supportingFiles.add(new SupportingFile("gwtIncompatible.mustache", (this.sourceFolder + File.separator + this.modelPackage().replace('.', File.separatorChar)).replace('/', File.separatorChar), "GwtIncompatible.java"));

    this.apiDocTemplateFiles.clear();
    this.apiTestTemplateFiles.clear();
    this.modelDocTemplateFiles.clear();
    this.modelTestTemplateFiles.clear();

    this.typeMapping.put("array", "JsArray");
    this.typeMapping.put("set", "JsSet");
    this.typeMapping.put("map", "JsMap");

    this.typeMapping.put("number", "JsNumber");
    this.typeMapping.put("decimal", "JsNumber");

    this.typeMapping.put("date", "String");
    this.typeMapping.put("DateTime", "String");
    this.typeMapping.put("UUID", "String");
    this.typeMapping.put("file", "String");


    this.importMapping.put("JsArray", "elemental2.core.JsArray");
    this.importMapping.put("JsMap", "elemental2.core.JsMap");
    this.importMapping.put("JsSet", "elemental2.core.JsSet");
    this.importMapping.put("JsNumber", "elemental2.core.JsNumber");

    this.instantiationTypes.put("array", "JsArray");
    this.instantiationTypes.put("set", "JsSet");
    this.instantiationTypes.put("map", "JsMap");

    this.importMapping.put("Promise", "elemental2.promise.Promise");
  }

  @Override
  protected ImmutableMap.Builder<String, Mustache.Lambda> addMustacheLambdas() {
    return super.addMustacheLambdas()
            .put("escapeAsterisk", new EscapeAsteriskLambda())
            .put("camelCase", new CamelCaseLambda(false));
  }

  @Override
  public CodegenModel fromModel(String name, Schema model) {
    CodegenModel result = super.fromModel(name, model);

    result.imports.remove("ApiModel");
    result.imports.remove("JsonNullable");

    return result;
  }

  @Override
  public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
    super.postProcessModelProperty(model, property);

    model.imports.remove("ApiModelProperty");
    model.imports.remove("ApiModel");
    model.imports.remove("JsonSerialize");
    model.imports.remove("ToStringSerializer");
  }

  @Override
  public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
    super.postProcessOperationsWithModels(objs, allModels);

    OperationsMap updatedObjs = postProcessOperations(objs);
    OperationMap operations = updatedObjs.getOperations();
    if (operations != null) {
      List<CodegenOperation> ops = operations.getOperation();
      for (CodegenOperation co : ops) {
        handleImplicitHeaders(co);

        co.imports.remove("ApiModel");
      }
    }
    return updatedObjs;
  }

  static OperationsMap postProcessOperations(OperationsMap objs) {
    OperationMap operations = objs.getOperations();
    String commonPath = null;
    if (operations != null) {
      List<CodegenOperation> ops = operations.getOperation();
      for (CodegenOperation operation : ops) {
        if (operation.hasConsumes == Boolean.TRUE) {
          Map<String, String> firstType = operation.consumes.get(0);
          if (firstType != null) {
            if ("multipart/form-data".equals(firstType.get("mediaType"))) {
              operation.isMultipart = Boolean.TRUE;
            }
          }
        }

        boolean isMultipartPost = false;
        List<Map<String, String>> consumes = operation.consumes;
        if (consumes != null) {
          for (Map<String, String> consume : consumes) {
            String mt = consume.get("mediaType");
            if (mt != null) {
              if (mt.startsWith("multipart/form-data")) {
                isMultipartPost = true;
              }
            }
          }
        }

        for (CodegenParameter parameter : operation.allParams) {
          if (isMultipartPost) {
            parameter.vendorExtensions.put("x-multipart", "true");
          }
        }

        List<CodegenResponse> responses = operation.responses;
        if (responses != null) {
          for (CodegenResponse resp : responses) {
            if ("0".equals(resp.code)) {
              resp.code = "200";
            }

            if (resp.baseType == null) {
              resp.dataType = "Void";
              resp.baseType = "Void";
              // set vendorExtensions.x-java-is-response-void to true as baseType is set to "Void"
              resp.vendorExtensions.put("x-java-is-response-void", true);
            }

            if ("array".equals(resp.containerType)) {
              resp.containerType = "List";
            } else if ("set".equals(resp.containerType)) {
              resp.containerType = "Set";
            } else if ("map".equals(resp.containerType)) {
              resp.containerType = "Map";
            }
          }
        }

        if (operation.returnBaseType == null) {
          operation.returnType = "Void";
          operation.returnBaseType = "Void";
          // set vendorExtensions.x-java-is-response-void to true as returnBaseType is set to "Void"
          operation.vendorExtensions.put("x-java-is-response-void", true);
        }

        if ("array".equals(operation.returnContainer)) {
          operation.returnContainer = "List";
        } else if ("set".equals(operation.returnContainer)) {
          operation.returnContainer = "Set";
        } else if ("map".equals(operation.returnContainer)) {
          operation.returnContainer = "Map";
        }

        if (commonPath == null) {
          commonPath = operation.path;
        } else {
          commonPath = getCommonPath(commonPath, operation.path);
        }
      }
      for (CodegenOperation co : ops) {
        co.path = StringUtils.removeStart(co.path, commonPath);
        co.subresourceOperation = co.path.length() > 1;
      }
      objs.put("commonPath", "/".equals(commonPath) ? StringUtils.EMPTY : commonPath);
    }
    return objs;
  }

  private static String getCommonPath(String path1, String path2) {
    final String[] parts1 = StringUtils.split(path1, "/");
    final String[] parts2 = StringUtils.split(path2, "/");
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
      if (!parts1[i].equals(parts2[i])) {
        break;
      }
      builder.append("/").append(parts1[i]);
    }
    return builder.toString();
  }

  private static class EscapeAsteriskLambda implements Mustache.Lambda {
    private EscapeAsteriskLambda() {
    }

    public void execute(Template.Fragment fragment, Writer writer) throws IOException {
      writer.write(fragment.execute().replace("*/", "*<code></code>/"));
    }
  }
}
