ThirdPartyNotices
-----------------
This project uses third-party software or other resources that
may be distributed under licenses different from this software.
In the event that we overlooked to list a required notice, please bring this
to our attention by contacting us via this email:
opensource.compliance-tooling@orange.com

ThirdParty Licenses
-----------------

You can generate an up-to-date license report using following command

```[shell]
 ./gradlew generateLicenseReport
 ```

Report will be generated [Here](build/reports/dependency-license/index.html)

```{r, echo=false}
htmltools::includeHTML(./build/reports/dependency-license/index.html)
```