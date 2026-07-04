# Prompt 2026-06-04:

Analyze the screenshot for formatting. Use the provided skill.


![](../assets/ScreenshotBlogpostCodecentric.jpg)

[Skill](../Skills/WebappStyleExtractorSkill/SKILL.md)
[References-Schema](../Skills/WebappStyleExtractorSkill/references/schema.md)
[Script](../Skills/WebappStyleExtractorSkill/scripts/probe.py)

# Answer:

Done — I extracted the screenshot’s formatting into a validated JSON style spec using the uploaded extractor workflow, schema, and probe script.

[Download the JSON style extraction](./CodecentricBlogpostStyleChatGpt55High.json)

Key findings:

* **Scale factor:** assumed `1×` CSS px.
* **Main palette:** white background `#FFFFFF`, chip surface `#F5F6F8`, strong text `#000000`, body/heading grey `#565759`, muted label `#9FA0A2`, input border `#DBDEE3`.
* **Search input:** `1969×70px`, `3px` border, `12px` radius, large left icon/text padding.
* **Topic chips:** `61px` tall, `#F5F6F8` fill, `5px` radius, about `40px` horizontal padding.
* **Blog grid:** 3-column text-only layout, approximately `560px` columns with about `90px` gaps.
* **Typography:** likely sans-serif stack; card titles around `36px / 44px`, excerpts around `26px / 40px`.

Caveats are included in the JSON: the font family is a visual guess, only the default state is visible, and the floating widgets are partially cropped.
