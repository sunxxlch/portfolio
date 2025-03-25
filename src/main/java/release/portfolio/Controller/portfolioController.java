package release.portfolio.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import release.portfolio.Model.DTO.Reports;
import release.portfolio.Model.DTO.defectandbugs;
import release.portfolio.Model.DTO.portfolioRequest;
import release.portfolio.Model.portfolioData;
import release.portfolio.Model.portfolioDataSets;
import release.portfolio.Services.portfolioService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin("http://localhost:3000/")
public class portfolioController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private portfolioService portservice;

    @GetMapping("/portfolios/{projectname}")
    public List<portfolioData> getportfoliosfromproject(@PathVariable String projectname){

        return portservice.getporfolios(projectname);
    }

    @PostMapping("/addPortfolio")
    public ResponseEntity<String> addportfolio(@RequestBody portfolioRequest request){
        portservice.addportfolio(request);
        return ResponseEntity.ok("Portfolio and datasets added successfully!");
    }


    @GetMapping("/portfoliodetails/{projectname}/{portfoliokey}")
    public List<portfolioDataSets> getportfoliodetails(@PathVariable String projectname,@PathVariable String portfoliokey){
        System.out.println(projectname+"-"+portfoliokey);
        return portservice.getdetailsofportfolio(projectname,portfoliokey);
    }

    @GetMapping("/uiportfoliodetails/{projectname}/{portfoliokey}")
    public List<Map<String, Object>> extractUiporfoliodetails(@PathVariable String projectname, @PathVariable String portfoliokey){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);

//        for(portfolioDataSets dt: data) {
//            boolean checkexcutionData = portservice.getexecutionData(dt.getId());
//
//            if(checkexcutionData==false){
//                portservice.setexecutionsData(dt.getId());
//            }
//        }

        return data.stream()
                .map(dataset -> Map.of(
                        "name", dataset.getSetName(),
                        "Pass", dataset.getExecutedData().get("Pass"),
                        "Fail", dataset.getExecutedData().get("Fail"),
                        "Unexecuted", dataset.getExecutedData().get("Unexecuted"),
                        "WIP", dataset.getExecutedData().get("WIP")
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/defectdetails/{projectname}/{portfoliokey}")
    public ArrayList<JsonNode> getdefects(@PathVariable String projectname, @PathVariable String portfoliokey){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);
        List<JsonNode> flatList = data.stream()
                .map(portfolioDataSets::getDefects) // Extract JsonNode
                .filter(Objects::nonNull) // Avoid null values
                .flatMap(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false)) // Convert JsonNode to Stream
                .collect(Collectors.toList());

        Map<String, JsonNode> uniqueDefects = new LinkedHashMap<>();
        for (JsonNode defect : flatList) {
            String key = defect.get("key").asText();
            uniqueDefects.putIfAbsent(key, defect);
        }

        return new ArrayList<>(uniqueDefects.values());
    }

    @DeleteMapping("/defectdetailsDelete/{projectname}/{portfoliokey}/{key}")
    public ArrayList<JsonNode> deleteDefects(@PathVariable String projectname, @PathVariable String portfoliokey , @PathVariable String key){
        portservice.deleteDefect(projectname,portfoliokey,key);
        return getdefects(projectname,portfoliokey);

    }

    @PostMapping("/defectdetailsAdd/{projectname}/{portfoliokey}/{pname}")
    public ArrayList<JsonNode> addDefects(@PathVariable String projectname, @PathVariable String portfoliokey ,@PathVariable String pname, @RequestBody defectandbugs dab){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);
        data.forEach(dt->{
            if(dt.getSetName().equals(pname)){
                portservice.addDefect(dt.getId(),dab.getKey(),dab.getValue());
            }
        });

        return getdefects(projectname,portfoliokey);

    }

    @GetMapping("/bugdetails/{projectname}/{portfoliokey}")
    public ArrayList<JsonNode> getbugs(@PathVariable String projectname, @PathVariable String portfoliokey){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);
        List<JsonNode> flatList = data.stream()
                .map(portfolioDataSets::getBugs) // Extract JsonNode
                .filter(Objects::nonNull) // Avoid null values
                .flatMap(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false)) // Convert JsonNode to Stream
                .collect(Collectors.toList());

        Map<String, JsonNode> uniqueDefects = new LinkedHashMap<>();
        for (JsonNode defect : flatList) {
            String key = defect.get("key").asText();
            uniqueDefects.putIfAbsent(key, defect);
        }

        return new ArrayList<>(uniqueDefects.values());
    }

    @DeleteMapping("/bugdetailsDelete/{projectname}/{portfoliokey}/{key}")
    public ArrayList<JsonNode> deletebugs(@PathVariable String projectname, @PathVariable String portfoliokey , @PathVariable String key){
        portservice.deleteBug(projectname,portfoliokey,key);
        return getbugs(projectname,portfoliokey);

    }

    @PostMapping("/bugdetailsAdd/{projectname}/{portfoliokey}/{pname}")
    public ArrayList<JsonNode> addbugs(@PathVariable String projectname, @PathVariable String portfoliokey ,@PathVariable String pname, @RequestBody defectandbugs dab){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);
        data.forEach(dt->{
            if(dt.getSetName().equals(pname)){
                portservice.addBug(dt.getId(),dab.getKey(),dab.getValue());
            }
        });

        return getbugs(projectname,portfoliokey);

    }

    @GetMapping("/executionReportdetails/{projectname}/{portfoliokey}/{pname}")
    public JsonNode getExecutionReportforEach(@PathVariable String projectname, @PathVariable String portfoliokey, @PathVariable String pname){
        return portservice.getportfoliodata(projectname,portfoliokey,pname);

    }

    @DeleteMapping("/executionReportdetails/{projectname}/{portfoliokey}/{pname}")
    public void deletereportforeach(@PathVariable String projectname, @PathVariable String portfoliokey , @PathVariable String pname,@RequestBody Reports rp){
        portservice.deletreport(projectname,portfoliokey,pname,rp);
    }

    @PostMapping("/executionReportdetails/{projectname}/{portfoliokey}/{pname}")
    public void addreports(@PathVariable String projectname, @PathVariable String portfoliokey , @PathVariable String pname,@RequestBody Reports rp){
        portservice.addreport(projectname,portfoliokey,pname,rp);

    }


    //for jira data adding
    @PutMapping("/updateDetails/{pid}")
    public Object updateDataofportfolio(@PathVariable long pid , @RequestBody portfolioDataSets pdsts){
        System.out.println(pid+"-"+pdsts);
        boolean dataupdated= portservice.updatedetailofid(pid,pdsts);
        if(dataupdated){
            return portservice.getupdateIddetails(pid);
        }else{
            return "details not updated";
        }

    }


    @PutMapping("/updateportfolioDetails/{pname}/{pkey}")
    public Object updateportfolioDetails(@PathVariable String pname,@PathVariable String pkey  ,@RequestBody portfolioRequest request){
        boolean updatedetais = portservice.updatealldetails(pname,pkey,request);
        if(updatedetais){
            return "details update  ";
        }else{
            return "details not updated";
        }
    }

    @DeleteMapping("/deletePortfolio/{pid}")
    public Object deletePortfolio(@PathVariable long pid){

        if(portservice.deletepprtfolioBybody(pid)){
            return "deleted";
        }else{
            return "not deleted";
        }
    }

    @DeleteMapping("/deletePortfolioSet/{pid}")
    public Object deleteportfolioSet(@PathVariable long pid){

        if(portservice.deletepprtfolioSet(pid)){
            return "deleted";
        }else{
            return "not deleted";
        }
    }

    @PostMapping("/addNewSet")
    public void addnewset(@RequestBody portfolioRequest pdst){
        System.out.println(pdst);
        portservice.addNewSetofData(pdst);
    }

    @GetMapping("/RefreshData/{projectname}/{portfoliokey}")
    public List<Map<String, Object>> refreshPortfolioData(@PathVariable String projectname, @PathVariable String portfoliokey){
        List<portfolioDataSets> data =portservice.getdetailsofportfolio(projectname,portfoliokey);

        for(portfolioDataSets dt: data) {

                portservice.setexecutionsData(dt.getId());
        }

        List<portfolioDataSets> data2 =portservice.getdetailsofportfolio(projectname,portfoliokey);
        System.out.println(data2);
        return data2.stream()
                .map(dataset -> Map.of(
                        "name", dataset.getSetName(),
                        "Pass", dataset.getExecutedData().get("Pass"),
                        "Fail", dataset.getExecutedData().get("Fail"),
                        "Unexecuted", dataset.getExecutedData().get("Unexecuted"),
                        "WIP", dataset.getExecutedData().get("WIP")
                ))
                .collect(Collectors.toList());
    }

}
