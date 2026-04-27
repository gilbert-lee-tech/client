# Multi-Brand Vehicle Feature Tile

## Key Features

- Clean Sling Model with a sensible injector strategy
- Accessibility-first HTL (semantic HTML, justified ARIA use, correct alt handling)
- A Granite dialog that's genuinely usable by an author
- Brand-aware theming without per-brand component forks
- TypeScript (not plain JS) for any client-side behavior

### Clean Sling Model with a sensible injector strategy

- Sling Model with annotations (@ValueMapValue)

### Accessibility-first HTL (semantic HTML, justified ARIA use, correct alt handling)

- use \<header>, \<nav>, \<section>, \<article>, etc
- use Site Audit for scanning
- \<article> over \<div>
  - identifies content for screen readers
  - designed for self-contained content
  - help search engine to understand the content
- for \<img>
  - alt="image description"
- for \<a href>
  - aria-label="xxxxx (opens in new tab)"
  - rel="noopener noreferrer"

### A Granite dialog that's genuinely usable by an author

- author-friendly UX
- fieldTitle and fieldDescription
- Use Tab
- Group similar fields in well
- Text place holder

### Brand-aware theming without per-brand component forks

The brand name recognition play the major factor of the project architecture.
The brand name can be derived by

#### Domain name, or URL

> #### Not a good idea.
>
> It can be re-written by CDN, or Web server.
>
> Difficult for the development testing

#### page properties

> #### Good idea.
>
> Increase the workload for the content author. The default value can be defined in the page initial content.
>
> Easy to mock for the development testing

#### Template Path

> #### Good idea.
>
> Create separated sites for each brand. Each brand has separated set of Editable Templates and Page Templates. Content Author pick the right template without making human mistake.
>
> Easy to mock for the development testing

## ASSUMPTION:

- It is a Multi Brands Site and only support one language and one region (us-en)
- Each brand has it's own header, footer, style, and structure.

## Site and Template structure

        /conf/
        ├── brand-a/settings/wcm/templates
        ├── brand-b/settings/wcm/templates
        ├── brand-c/settings/wcm/templates
        └── brand-d/settings/wcm/templates

## Part 2: Reusability Writeup

In your README (max one page), briefly explain:

1. How you'd keep four brands rendering from one component without per-brand forks
2. Where brand-specific CSS lives (clientlib structure)
3. What you would place in the shared core module vs. each brand's ui.apps

# Reusability Writeup

1.  The brand can be derived from the Page Properties (itself, or inheritedPageProperties), or from the template path (SITE name). We don't need proxy component, or per-brand forks.
2.       └── clientlibs/
             ├── clientlib-base/            <-- Global styles with categories 'brand.base'
             ├── clientlib-brand-a/         <-- Brand A with categories 'brand.a', with dependencies 'brand.base'
             ├── clientlib-brand-a/         <-- Brand B with categories 'brand.b', with dependencies 'brand.base'
             ├── clientlib-brand-a/         <-- Brand C with categories 'brand.c', with dependencies 'brand.base'
             └── clientlib-brand-b/         <-- Brand D with categories 'brand-d', with dependencies 'brand.base'

3.  We won't put it into the core module. The core module designs for sharing common logic. For the different layout and design, brand's ui.api is more ideal.

# Bonus

- Headless variant: sketch a Content Fragment Model for "Vehicle Feature" and a GraphQL query to fetch all features for a given vehicle model
  - We need an extra vehicle model for the above vehicle feature.

| Field        | Label              | Type        | Required |
| ------------ | ------------------ | ----------- | -------- |
| model        | Model              | Single-line | yes      |
| title        | Title              | Single-line | yes      |
| description  | Description        | Multi-line  | yes      |
| iconPath     | Icon Path          | Single-line | yes      |
| ctaLabel     | CTA Label          | Single-line | no       |
| ctaUrl       | CTA URL            | Single-line | no       |
| ctaNewWindow | Open in new window | Boolean     | no       |
| ctaAriaLabel | CTA Aria           | Single-line | no       |

```json
query VehicleFeatureByModel($model: String!) {
    VehicleFeatureList(
      filter: {
        model: {
          _expressions: [{ value: $model, _operator: EQUALS }]
        }
      }
    ) {
      items {
        model
        title
        description
        iconPath
        ctaLabel
        ctaUrl
        ctaNewWindow
        ctaAriaLabel
      }
    }
  }
```

- Dispatcher caching: one paragraph on TTL and invalidation strategy when a feature description changes
  - In normal case, after the feature description updated, author will publish the page. After that, the AEM author instance triggers the cache invalidation to all publish instances. We can configure the enableTTL on the dispatcher. We may decide that a TTL of 5 minutes for .html content is reasonable, but cached .js and .css files should live in the cache for 24 hours

- Cloud Manager: identify two quality-gate failures this component might trip (code coverage, SonarQube, custom rules) and how you'd address them pre-merge
  - The code coverage failures can be addressed by the Unit Tests coverage report. The report will be generated, after the developer build the project locally.
  - The SonarQube failures can be addressed by downloading the Adobe Full SonarQube rules and install them into developer's IDE.
