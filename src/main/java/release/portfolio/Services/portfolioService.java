package release.portfolio.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import release.portfolio.Dao.HotspotRepo;
import release.portfolio.Dao.adminRepo;
import release.portfolio.Dao.portfolioDataSetRepo;
import release.portfolio.Dao.portfolioRepo;
import release.portfolio.Model.AdminData;
import release.portfolio.Model.DTO.Reports;
import release.portfolio.Model.DTO.portfolioDataSetsRequest;
import release.portfolio.Model.DTO.portfolioRequest;
import release.portfolio.Model.hotspot;
import release.portfolio.Model.portfolioData;
import release.portfolio.Model.portfolioDataSets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class portfolioService {

    @Autowired
    private portfolioRepo portrepo;

    @Autowired
    private portfolioDataSetRepo pdatarepo;

    @Autowired
    private adminRepo adrepo;

    @Autowired
    private HotspotRepo hsrepo;

    @Autowired
    private emailService email;

    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void checkPortfolios() {

        List<hotspot> lsthotspots = hsrepo.findAll();
        if (lsthotspots.size() != 0) {

            for (hotspot hp : lsthotspots) {
                List<portfolioDataSets> data = getdetailsofportfolio(hp.getProjectName(), hp.getPortfolioKey());
                int success = 0;

                for (portfolioDataSets dt : data) {

                    JsonNode js = (JsonNode) refreshAndCheckExecutionsData(dt.getId());
                    int pass = js.get("Pass").asInt();
                    int fail = js.get("Fail").asInt();
                    int wip = js.get("WIP").asInt();
                    int unexecuted = js.get("Unexecuted").asInt();

                    if (pass > 0 && (fail == 0 && wip == 0 && unexecuted == 0)) {
                        success++;

                    }

                }
                if (success == data.size()) {
                    email.sendEmail("Release Sign Off", hp.getProjectName(), hp.getPortfolioKey());
                    deleteHotspot(hp.getProjectName(), hp.getPortfolioKey());

                } else {
                    LocalDate dor = hp.getDor().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (dor.plusDays(1).isEqual(LocalDate.now())) {
                        email.sendEmail("Conditional Sign Off", hp.getProjectName(), hp.getPortfolioKey());
                        deleteHotspot(hp.getProjectName(), hp.getPortfolioKey());
                    }
                }

            }
        }
    }

    public void deleteHotspot(String projectName, String portfolioKey){
        Optional<hotspot> hotspotOptional = hsrepo.findByProjectNameAndPortfolioKey(projectName, portfolioKey);
        if (hotspotOptional.isPresent()) {
            hsrepo.delete(hotspotOptional.get());
        }
    }

    @Transactional
    public Object refreshAndCheckExecutionsData(Long id) {

        Optional<portfolioDataSets> pdsts = pdatarepo.findById(id);

        if (pdsts.isPresent()) {
            portfolioDataSets portdetails = pdsts.get();

            Optional<AdminData> opadm = adrepo.findById(portdetails.getProjectName());
            AdminData ad = opadm.get();
            String projectId = portdetails.getProjectId();
            String cycleId = portdetails.getCycleId();
            String versionId = portdetails.getVersionId();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode executionData = fetchDataFromApi(projectId, cycleId,versionId,portdetails.getSetName(),ad.getCredentials());

            portdetails.setExecutedData(executionData);
            pdatarepo.save(portdetails);

            System.out.println("Execution data updated successfully!");
            return executionData;
        } else {

            throw new RuntimeException("PortfolioDataSet not found with ID: " + id);

        }

    }


    @Transactional
    public void addportfolio(portfolioRequest request) {
        if(!portrepo.existsByPortfolioKeyAndProjectName(request.getPortfolioKey(),request.getProjectName())){
            portfolioData pdata = new portfolioData();
            pdata.setProjectName(request.getProjectName());
            pdata.setPortfolioKey(request.getPortfolioKey());
            portrepo.save(pdata);
        }
        String pkey = request.getPortfolioKey();
        if(pkey.length()>8){
            pkey= pkey.substring(pkey.length()-8,pkey.length());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                Date dor = sdf.parse(pkey);
                hotspot hsp = new hotspot();
                hsp.setProjectName(request.getProjectName());
                hsp.setPortfolioKey(request.getPortfolioKey());
                hsp.setDor(dor);
                hsrepo.save(hsp);

            } catch (ParseException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        List<portfolioDataSets> portdatasets= request.getDataSets().stream().map(datasets->{
            portfolioDataSets pdst = new portfolioDataSets();
            pdst.setPortfolioKey(request.getPortfolioKey());
            pdst.setSetName(datasets.getSetName());
            pdst.setProjectId(datasets.getProjectId());
            pdst.setVersionId(datasets.getVersionId());
            pdst.setCycleId(datasets.getCycleId());
            pdst.setProjectName(request.getProjectName());
            return pdst;
        }).toList();

        pdatarepo.saveAll(portdatasets);

    }

    public List<portfolioDataSets> getdetailsofportfolio(String pname,String pkey) {
        return pdatarepo.findByPortfolioKeyAndProjectName(pkey,pname);
    }

    @Transactional
    public boolean updatedetailofid(long pid,portfolioDataSets portdts) {

        Optional<portfolioDataSets> pdsts = pdatarepo.findById(pid);

        if(pdsts.isPresent()){
            portfolioDataSets portdetails = pdsts.get();
            portdetails.setDefects(portdts.getDefects());
            portdetails.setBugs(portdts.getBugs());
            portdetails.setExecutedData(portdts.getExecutedData());
            pdatarepo.save(portdetails);
            return true;
        }else{
            return false;
        }
    }

    public Optional<portfolioDataSets> getupdateIddetails(long pid) {

        return pdatarepo.findById(pid);

    }

    public List<portfolioData> getporfolios(String projectname) {
        return portrepo.findByProjectName(projectname);
    }

    public boolean getexecutionData(Long id) {
        Optional<portfolioDataSets> pdsts = pdatarepo.findById(id);
        portfolioDataSets portdetails = pdsts.get();
        if(portdetails.getExecutedData()==null) {
            return false;
        }else{
            return true;
        }
    }

    @Transactional
    public void setexecutionsData(Long id) {

        Optional<portfolioDataSets> pdsts = pdatarepo.findById(id);

        if (pdsts.isPresent()) {
            portfolioDataSets portdetails = pdsts.get();

            Optional<AdminData> opadm = adrepo.findById(portdetails.getProjectName());
            AdminData ad = opadm.get();
            String projectId = portdetails.getProjectId();
            String cycleId = portdetails.getCycleId();
            String versionId = portdetails.getVersionId();

            JsonNode executionData = fetchDataFromApi(projectId, cycleId,versionId,portdetails.getSetName(),ad.getCredentials());

            portdetails.setExecutedData(executionData);
            pdatarepo.save(portdetails);

            System.out.println("Execution data updated successfully!");
        } else {
            throw new RuntimeException("PortfolioDataSet not found with ID: " + id);
        }
    }



    public JsonNode fetchDataFromApi(String projectId, String cycleId, String versionId,String setname, String creds) {

        String jiraUrl = "https://jira.cengage.com/rest/zapi/latest/";
        String apiUrl = jiraUrl + "execution?projectId=" + projectId + "&versionId=" + versionId + "&cycleId=" + cycleId;
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(apiUrl);
        HttpHeaders headers = createHeaders(creds);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        System.out.println(response.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;

        int Pass = 0;
        int Fail = 0;
        int unExecuted = 0;
        int WIP =0;


        try {
            rootNode = mapper.readTree(response.getBody());
            JsonNode executions = rootNode.path("executions");

            for (int i = 0; i < executions.size(); i++) {
                String status  = executions.get(i).get("executionStatus").asText();

                switch (status) {
                    case "1":
                        Pass++;
                        break;
                    case "2":
                        Fail++;
                        break;
                    case "3":
                        WIP++;
                        break;
                    case "-1":
                        unExecuted++;
                        break;
                    default:
                        // Handle any other status codes if needed
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("name", setname);
            objectNode.put("Pass", Pass);
            objectNode.put("Fail",Fail);
            objectNode.put("Unexecuted", unExecuted);
            objectNode.put("WIP", WIP);

        return objectNode;
    }
    private HttpHeaders createHeaders(String creds) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + creds);
        return headers;
    }

    @Transactional
    public boolean updatealldetails(String pname,String pkey, portfolioRequest request) {
        List<portfolioDataSets> pdsts = pdatarepo.findByPortfolioKeyAndProjectName(pkey, pname);
        System.out.println(pdsts);
        if(!pdsts.isEmpty()){
            IntStream.range(0,pdsts.size()).forEach(i->{
                portfolioDataSets pdsts1 = pdsts.get(i);
                portfolioDataSetsRequest req1 = request.getDataSets().get(i);

                pdsts1.setSetName(req1.getSetName());
                pdsts1.setVersionId(req1.getVersionId());
                pdsts1.setCycleId(req1.getCycleId());
                pdsts1.setProjectId(req1.getProjectId());

            });

            pdatarepo.saveAll(pdsts);
            return true;
        }else{
            return false;
        }

    }

    @Transactional
    public boolean deletepprtfolioBybody(long pid) {
        portfolioData pd = portrepo.findById(pid);

        if(pd.getId()==pid){
            portrepo.deleteById(pid);
            return true;
        }else{
            return  false;
        }
    }

    public boolean deletepprtfolioSet(long pid) {
        Optional<portfolioDataSets> pd = pdatarepo.findById(pid);
        if(pd.isPresent()){
            pdatarepo.deleteById(pid);
            return true;
        }else{
            return  false;
        }
    }

    public void deleteDefect(String key, List<JsonNode> list) {


    }

    public void deleteDefect(String projectname, String portfoliokey, String key) {
        List<portfolioDataSets> temp = pdatarepo.findByPortfolioKeyAndProjectName(portfoliokey, projectname);

        if (temp.isEmpty()) {
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        temp.forEach(pdts -> {
            JsonNode defectsNode = pdts.getDefects();

            if (defectsNode != null && defectsNode.isArray()) {
                ArrayNode defectsArray = (ArrayNode) defectsNode;
                ArrayNode updatedDefects = objectMapper.createArrayNode();
                defectsArray.forEach(defect -> {
                    if (defect.has("key") && !defect.get("key").asText().equals(key)) {
                        updatedDefects.add(defect); // Keep only non-matching defects
                    }
                });
                pdts.setDefects(updatedDefects);
                pdatarepo.save(pdts);
            }
        });
    }

    public void addDefect(long pid, String key,String url) {
        Optional<portfolioDataSets> temp = pdatarepo.findById(pid);
        if (temp.isEmpty()) {
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }

        portfolioDataSets pdst = temp.get();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode defectsNode = pdst.getDefects();
        ArrayNode defectsArray;
        if (defectsNode != null && defectsNode.isArray()) {
            defectsArray = (ArrayNode) defectsNode;
        } else {
            defectsArray = objectMapper.createArrayNode();
        }

        ObjectNode newDefect = objectMapper.createObjectNode();
        newDefect.put("key", key);
        newDefect.put("value", url);

        defectsArray.add(newDefect);
        pdst.setDefects(defectsArray);
        pdatarepo.save(pdst);

    }

    public void deleteBug(String projectname, String portfoliokey, String key) {
        List<portfolioDataSets> temp = pdatarepo.findByPortfolioKeyAndProjectName(portfoliokey, projectname);

        if (temp.isEmpty()) {
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        temp.forEach(pdts -> {
            JsonNode defectsNode = pdts.getBugs();

            if (defectsNode != null && defectsNode.isArray()) {
                ArrayNode defectsArray = (ArrayNode) defectsNode;
                ArrayNode updatedDefects = objectMapper.createArrayNode();
                defectsArray.forEach(defect -> {
                    if (defect.has("key") && !defect.get("key").asText().equals(key)) {
                        updatedDefects.add(defect); // Keep only non-matching defects
                    }
                });
                pdts.setBugs(updatedDefects);
                pdatarepo.save(pdts);
            }
        });
    }

    public void addBug(long pid, String key,String url) {
        Optional<portfolioDataSets> temp = pdatarepo.findById(pid);
        if (temp.isEmpty()) {
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }

        portfolioDataSets pdst = temp.get();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode defectsNode = pdst.getBugs();
        ArrayNode defectsArray;
        if (defectsNode != null && defectsNode.isArray()) {
            defectsArray = (ArrayNode) defectsNode;
        } else {
            defectsArray = objectMapper.createArrayNode();
        }

        ObjectNode newDefect = objectMapper.createObjectNode();
        newDefect.put("key", key);
        newDefect.put("value", url);

        defectsArray.add(newDefect);
        pdst.setBugs(defectsArray);
        pdatarepo.save(pdst);

    }

    public JsonNode getportfoliodata(String projectname, String portfoliokey, String pname) {
        Optional<portfolioDataSets> pdt = pdatarepo.findByPortfolioKeyAndProjectNameAndSetName(portfoliokey,projectname,pname);
        if(pdt.isPresent()){
            portfolioDataSets data = pdt.get();
            return data.getReports();
        }else{
            return null;
        }
    }

    public void deletreport(String projectname, String portfoliokey, String pname, Reports rp) {
        Optional<portfolioDataSets> pdt = pdatarepo.findByPortfolioKeyAndProjectNameAndSetName(portfoliokey,projectname,pname);
        if(pdt.isEmpty()){
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }
        portfolioDataSets portdata = pdt.get();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(portdata.getReports());

        portfolioDataSets temp = portdata;

        JsonNode jsreport = portdata.getReports();

        if (jsreport != null && jsreport.isArray()) {
            ArrayNode reportArray = (ArrayNode) jsreport;
            ArrayNode updatedreports = objectMapper.createArrayNode();
            jsreport.forEach(rpunq -> {
                if (rpunq.has("name") && !rpunq.get("name").asText().equals(rp.getName())) {
                    updatedreports.add(rpunq); // Keep only non-matching defects
                }
            });
            temp.setReports(updatedreports);
            pdatarepo.save(temp);
        }

    }

    public void addreport(String projectname, String portfoliokey, String pname, Reports rp) {
        Optional<portfolioDataSets> pdt = pdatarepo.findByPortfolioKeyAndProjectNameAndSetName(portfoliokey, projectname, pname);
        if (pdt.isEmpty()) {
            throw new RuntimeException("No portfolio data found for given project and portfolio ID.");
        }

        portfolioDataSets pdst = pdt.get();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsdata = pdst.getReports();

        ArrayNode reportArray;
        if (jsdata != null && jsdata.isArray()) {
            reportArray = (ArrayNode) jsdata;
        } else {
            reportArray = objectMapper.createArrayNode();
        }

        ObjectNode newDefect = objectMapper.createObjectNode();
        newDefect.put("name", rp.getName());
        newDefect.put("url", rp.getUrl());

        reportArray.add(newDefect);
        pdst.setReports(reportArray);

        pdatarepo.save(pdst);
    }

    public void addNewSetofData(portfolioRequest pdst) {
        portfolioDataSets  data = new portfolioDataSets();
        data.setProjectName(pdst.getProjectName());
        data.setPortfolioKey(pdst.getPortfolioKey());
        List<portfolioDataSetsRequest> pr =  pdst.getDataSets();
        data.setSetName(pr.get(0).getSetName());
        data.setVersionId(pr.get(0).getVersionId());
        data.setCycleId(pr.get(0).getCycleId());
        data.setProjectId(pr.get(0).getProjectId());
        pdatarepo.save(data);
    }
}
