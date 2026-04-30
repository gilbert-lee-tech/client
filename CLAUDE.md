# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is an Adobe Experience Manager (AEM) multi-brand component project called "AEM Best Practices". It demonstrates clean architecture for building reusable AEM components that work across multiple brands without per-brand code forks.

**Key Focus**: Reusable AEM components with accessibility-first HTL, Sling Models with clean injection patterns, and author-friendly dialogs.

## Project Structure

The project uses a standard AEM Maven archetype structure with these key modules:

- **core/** - Java backend: Sling Models, Servlets, Filters, Listeners, Schedulers
- **ui.frontend/** - Frontend build system (Webpack, TypeScript, SCSS)
- **ui.apps/** - Component definitions (HTL templates, dialogs, clientlibs)
- **ui.apps.structure/** - Package structure for repository initialization
- **ui.config/** - OSGi configuration
- **ui.content/** - Sample content and initial data
- **ui.tests/** - Cypress end-to-end tests
- **it.tests/** - Java integration tests
- **dispatcher/** - Apache Dispatcher configuration
- **all/** - Aggregator package for all deployments

## Build & Commands

### Frontend (ui.frontend/)

**Setup**: `npm ci` (from ui.frontend directory)

**Development**:
- `npm run dev` - Build client libraries (no optimization, source maps enabled)
- `npm run prod` - Production build (minified, optimized, no source maps)
- `npm run start` - Start webpack-dev-server on localhost:8080 with proxy to AEM at localhost:4502
- `npm run watch` - Parallel: dev server + chokidar file watcher + aemsync file sync to AEM

**Single Test**: Frontend uses webpack + ESLint (no Jest). Linting is part of webpack build.
- `npm run lint` (if available in package.json - currently build includes ESLint plugin)

### Backend (core/)

**Unit Tests**: JUnit 5 with wcm.io mocking framework
- `mvn clean test` (from project root or core/)
- `mvn clean test -Dtest=MyModelTest` (single test class)
- Tests use `AemContext` and `AppAemContext` helper from `com.client.core.testcontext`

### Full Project

**Build & Deploy**:
- `mvn clean install` - Full build (builds all modules, runs all tests)
- `mvn -PautoInstallBundle clean install` - Build and install only the core bundle to AEM
- `mvn -PautoInstallPackage clean install` - Build and install full package to author instance (localhost:4502)
- `mvn -PautoInstallPackagePublish clean install` - Build and install to publish instance (localhost:4503)

**Maven Properties** (in root pom.xml):
- AEM host/port: localhost:4502 (author), localhost:4503 (publish)
- Default credentials: admin/admin

### Code Quality

- **ESLint** - TypeScript/JavaScript linting (webpack plugin runs during build)
- **SonarQube** - Part of Cloud Manager pipeline; check `.babelrc` and `.eslintrc.js` for tool configuration

## Architecture Patterns

### Frontend Architecture

**Entry Point**: `ui.frontend/src/main/webpack/site/main.ts`
- Imports all component JS via glob: `import '../components/**/*.js'`
- Imports all component SCSS via glob: `import './main.scss'`
- Compiles to two clientlibs via `clientlib.config.js`:
  - `clientlib-site` (all custom code) → `dist/clientlib-site/`
  - `clientlib-dependencies` (vendor code) → `dist/clientlib-dependencies/`

**Webpack Configuration**:
- Common config: `webpack.common.js` (resolvers, loaders, plugins)
- Dev config: `webpack.dev.js` (dev server, source maps, minimal optimization)
- Prod config: `webpack.prod.js` (minification via Terser + CssMinimizerPlugin)
- TypeScript loader: `ts-loader` with `glob-import-loader` for pattern-based imports
- SCSS pipeline: sass-loader → postcss (autoprefixer) → css-loader → MiniCssExtractPlugin

**Component JavaScript Pattern** (see `_helloworld.js`):
- Self-contained IIFE preventing global scope pollution
- Uses `data-cmp-is="component-name"` for DOM selection (not CSS selectors)
- Initializes on DOMContentReady + MutationObserver for dynamically added components
- Removes `data-cmp-is` after init to prevent re-initialization

**Client Library Generation**:
- `aem-clientlib-generator` reads `clientlib.config.js` after webpack build
- Moves compiled CSS/JS from `dist/` to `ui.apps/src/main/content/jcr_root/apps/client/clientlibs/`
- Runs as part of `npm run dev` / `npm run prod`

### Backend (Java/Sling Models)

**Sling Model Pattern** (see `HelloWorldModel.java`):
- Adaptable: `@Model(adaptables = Resource.class)`
- Injection: `@ValueMapValue` with `InjectionStrategy.OPTIONAL` and `@Default` for safety
- Template path extraction: `PageManager` → `getContainingPage()` → `getTemplate()` → `getPath()`
- Post-construct initialization: `@PostConstruct` method runs after field injection

**Testing Pattern**:
- Framework: JUnit 5 (`@ExtendWith(AemContextExtension.class)`)
- Mocking: `AemContext` from wcm.io (see `AppAemContext.java` for setup)
- Resource creation: `context.create().resource()` with property map
- Model adaptation: `resource.adaptTo(MyModel.class)`
- Coverage: Properties, methods, edge cases (empty properties), utility logic

## Architecture

### Module Responsibilities

| Module        | Purpose                                                                            |
| ------------- | ---------------------------------------------------------------------------------- |
| `core`        | Java bundle: Sling Models, OSGi services, servlets, filters, schedulers, listeners |
| `ui.apps`     | JCR `/apps` content: HTL templates, component definitions, clientlib structure     |
| `ui.frontend` | Webpack build producing JS/CSS → output goes into `ui.apps` clientlibs             |
| `ui.config`   | OSGi configurations (`.cfg.json` files), split by run mode                         |
| `ui.content`  | Sample/initial JCR content under `/content`                                        |
| `all`         | Single `content-package` that embeds all other packages for deployment             |
| `it.tests`    | Java integration tests (`*IT.java`) using AEM Testing Clients                      |
| `ui.tests`    | Cypress UI tests                                                                   |

### Sling Models (`core/src/main/java/.../models/`)

Models use `@Model(adaptables = Resource.class)` and initialize via `@PostConstruct`. Standard injectors:

- `@ValueMapValue` — reads JCR properties from the resource
- `@SlingObject` — injects `Resource`, `ResourceResolver`, `SlingHttpServletRequest`, etc.
- `@OSGiService` — injects OSGi services
- `@Default` — fallback value when injection is optional

HTL templates reference models with `data-sly-use.model="com.demo.core.models.ClassName"`.

### HTL Template Pattern (`ui.apps/.../components/<name>/<name>.html`)

Every component HTL template must follow this init pattern:

1. Bind the Sling Model with `data-sly-use.model`
2. Declare `data-sly-test.isComplete` on the root element — AND all **required** fields from the model
3. Render a placeholder when `isComplete` is false, using the component name as the label

```html
<article class="cmp-<name>"
         data-sly-use.model="com.demo.core.models.<Name>Model"
         data-sly-test.isComplete="${model.requiredField1 && model.requiredField2}"
         data-cmp-is="<name>">

    <!-- render all model fields here -->
    <h2 class="cmp-<name>__title">${model.requiredField1}</h2>
    <p  class="cmp-<name>__body">${model.requiredField2}</p>

    <!-- optional fields guarded individually -->
    <div class="cmp-<name>__cta" data-sly-test="${model.optionalField}">
        ${model.optionalField}
    </div>

</article>

<!-- placeholder shown in author when required fields are missing -->
<sly data-sly-test="${!isComplete}">
    <p class="cmp-<name>__placeholder"><Name> Component</p>
</sly>
```

**Example:**

```html
<article
  class="cmp-tile ${model.themeClass}"
  data-sly-use.model="com.demo.core.models.TileModel"
  data-sly-test.isComplete="${model.title && model.description && model.iconPath}"
  data-cmp-is="tile"
>
  <div class="cmp-tile__icon-wrapper">
    <img
      class="cmp-tile__icon"
      src="${model.iconPath}"
      alt=""
      role="presentation"
    />
  </div>
  <h2 class="cmp-tile__text">${model.title}</h2>
  <p class="cmp-tile__description">${model.description}</p>

  <div class="cmp-tile__cta" data-sly-test="${model.ctaLabel && model.ctaUrl}">
    <a
      class="cmp-tile__cta-link"
      href="${model.ctaUrl}"
      target="${model.ctaNewWindow ? '_blank' : '_self'}"
      rel="${model.ctaNewWindow ? 'noopener noreferrer' : ''}"
      >${model.ctaLabel}</a
    >
  </div>
</article>
<sly data-sly-test="${!isComplete}">
  <p class="cmp-tile__placeholder">Tile Component</p>
</sly>
```

**Rules:**

- `isComplete` must reference every field marked `required="{Boolean}true"` in the dialog
- Optional fields are guarded with their own `data-sly-test` — never included in `isComplete`
- The placeholder text is always `"<Component Display Name> Component"` (matches `jcr:title` in `.content.xml`)

### OSGi Services / Schedulers (`core/src/main/java/.../schedulers/`)

Configurable services use `@Designate(ocd=Config.class)` + `@ObjectClassDefinition`. Configuration is picked up from OSGi console (`/system/console/configMgr`) or from `.cfg.json` files in `ui.config`. Schedulers implement `Runnable` and use `scheduler_expression` (cron) and `scheduler_concurrent` properties.

### Servlets (`core/src/main/java/.../servlets/`)

Registered via `@SlingServletResourceTypes` annotation (no XML registration needed). Use `SlingSafeMethodsServlet` for GET-only, `SlingAllMethodsServlet` for write operations.

### OSGi Configuration Files (`ui.config/`)

Placed under `src/main/content/jcr_root/apps/demo/osgiconfig/`:

- `config/` — applies to all run modes
- `config.author/` — author only
- `config.publish/` — publish only

Factory configurations use tilde naming: `com.example.ServiceImpl~myname.cfg.json`.

### Component Structure (`ui.apps/`)

Components live at `jcr_root/apps/demo/components/<name>/`. Each component has:

- `.content.xml` — declares `jcr:primaryType="cq:Component"`, `componentGroup`, and optionally `sling:resourceSuperType`
- `<name>.html` — HTL template
- `_cq_dialog/.content.xml` — Touch UI dialog definition

The `page` component extends `core/wcm/components/page/v3/page` via `sling:resourceSuperType`.

### Author Dialog (`ui.apps/.../components/<name>/_cq_dialog/.content.xml`)

All dialogs use a **single column** layout (`fixedcolumns` + one `container`) — no tabs.

Every field must include `fieldLabel`, `fieldDescription`, and `emptyText`. Mark `required="{Boolean}true"` on any field whose Sling model property has no meaningful `@Default` (i.e. it is part of `isCompleted` in the HTL template).

**Template:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
          xmlns:cq="http://www.day.com/jcr/cq/1.0"
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    jcr:primaryType="nt:unstructured"
    jcr:title="<Component Display Name>"
    sling:resourceType="cq/gui/components/authoring/dialog">
    <content
        jcr:primaryType="nt:unstructured"
        sling:resourceType="granite/ui/components/coral/foundation/fixedcolumns">
        <items jcr:primaryType="nt:unstructured">
            <column
                jcr:primaryType="nt:unstructured"
                sling:resourceType="granite/ui/components/coral/foundation/container">
                <items jcr:primaryType="nt:unstructured">

                    <!-- required text field -->
                    <fieldName
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
                        fieldLabel="Field Label"
                        fieldDescription="Help text shown below the field."
                        emptyText="e.g. placeholder text"
                        name="./fieldName"
                        required="{Boolean}true"/>

                    <!-- optional textarea -->
                    <optionalField
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/textarea"
                        fieldLabel="Optional Field"
                        fieldDescription="Help text shown below the field."
                        emptyText="e.g. placeholder text"
                        name="./optionalField"/>

                    <!-- optional DAM path picker -->
                    <imagePath
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/pathfield"
                        fieldLabel="Image"
                        fieldDescription="Select an image from the DAM."
                        emptyText="e.g. /content/dam/image.png"
                        rootPath="/content/dam"
                        name="./imagePath"/>

                    <!-- optional checkbox -->
                    <openNewWindow
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/checkbox"
                        text="Open in new window"
                        fieldDescription="Check to open the link in a new browser tab."
                        name="./openNewWindow"
                        value="{Boolean}true"
                        uncheckedValue="{Boolean}false"/>

                </items>
            </column>
        </items>
    </content>
</jcr:root>
```

**Field type reference:**

| Field type | `sling:resourceType` suffix |
|---|---|
| Single-line text | `form/textfield` |
| Multi-line text | `form/textarea` |
| DAM / page path | `form/pathfield` |
| Checkbox | `form/checkbox` |
| Select (dropdown) | `form/select` |
| Number | `form/numberfield` |

**Example:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
          xmlns:cq="http://www.day.com/jcr/cq/1.0"
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    jcr:primaryType="nt:unstructured"
    jcr:title="Tile"
    sling:resourceType="cq/gui/components/authoring/dialog">
    <content
        jcr:primaryType="nt:unstructured"
        sling:resourceType="granite/ui/components/coral/foundation/fixedcolumns">
        <items jcr:primaryType="nt:unstructured">
            <column
                jcr:primaryType="nt:unstructured"
                sling:resourceType="granite/ui/components/coral/foundation/container">
                <items jcr:primaryType="nt:unstructured">

                    <!-- required — part of isCompleted in tile.html -->
                    <title
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
                        fieldLabel="Title"
                        fieldDescription="Main heading displayed on the component."
                        emptyText="e.g. Welcome to our site"
                        name="./title"
                        required="{Boolean}true"/>

                    <!-- required — part of isCompleted in tile.html -->
                    <description
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/textarea"
                        fieldLabel="Description"
                        fieldDescription="Supporting text displayed below the title."
                        emptyText="e.g. We help teams build better products."
                        name="./description"
                        required="{Boolean}true"/>

                    <!-- required — part of isCompleted in tile.html -->
                    <iconPath
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/pathfield"
                        fieldLabel="Icon"
                        fieldDescription="SVG or PNG icon displayed above the title."
                        emptyText="e.g. /content/dam/icons/star.svg"
                        rootPath="/content/dam"
                        name="./iconPath"
                        required="{Boolean}true"/>

                    <!-- optional -->
                    <ctaLabel
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/textfield"
                        fieldLabel="CTA Label"
                        fieldDescription="Button label. Leave blank to hide the CTA."
                        emptyText="e.g. Learn more"
                        name="./ctaLabel"/>

                    <!-- optional -->
                    <ctaUrl
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/pathfield"
                        fieldLabel="CTA URL"
                        fieldDescription="Destination page or external URL for the CTA button."
                        emptyText="e.g. /content/demo/en/about"
                        name="./ctaUrl"/>

                    <!-- optional -->
                    <ctaNewWindow
                        jcr:primaryType="nt:unstructured"
                        sling:resourceType="granite/ui/components/coral/foundation/form/checkbox"
                        text="Open in new window"
                        fieldDescription="Check to open the CTA link in a new browser tab."
                        name="./ctaNewWindow"
                        value="{Boolean}true"
                        uncheckedValue="{Boolean}false"/>

                </items>
            </column>
        </items>
    </content>
</jcr:root>
```

**Rules:**
- Use `fixedcolumns` + a single `column` container — never `tabs`
- Every field needs `fieldLabel`, `fieldDescription`, and `emptyText`
- `required="{Boolean}true"` — add only when the field is in `isCompleted` in the HTL template
- `name` attribute must match the Sling model field name exactly (prefix `./`)
- For `pathfield` pointing to DAM assets, always set `rootPath="/content/dam"`

### Testing Pattern

Unit tests use **JUnit 5 + wcm.io AEM Mocks**. Always use `AppAemContext.newAemContext()` (not bare `AemContext`) — it pre-registers the CACONFIG and CORE_COMPONENTS mock plugins.

## Development Workflow

1. **HTML/CSS/JS Changes**:
   - Edit files in `ui.frontend/src/main/webpack/`
   - `npm run dev` builds to `dist/`
   - `clientlib` (or `npm run prod` at end) copies to `ui.apps/src/main/content/jcr_root/apps/client/clientlibs/`
   - Sync to AEM: `npm run watch` (parallel start + chokidar + aemsync) or `aemsync -d -p ../ui.apps/src/main/content`

2. **Java Backend Changes**:
   - Edit in `core/src/main/java/`
   - Add tests in `core/src/test/java/`
   - `mvn clean test` validates locally
   - `mvn -PautoInstallPackage clean install` deploys to AEM with tests

3. **Component Dialog/Structure**:
   - Edit `.content.xml` files in `ui.apps/src/main/content/jcr_root/apps/client/components/[component]/`
   - HTL template in `[component]/[component].html`
   - Granite dialog in `_cq_dialog/.content.xml`

## Git Workflow

1. **Fetch the issue details** to understand the scope of work
2. **Create a branch** named after the issue — format: `issue-{number}-{short-description-in-kebab-case}`
   - Example: issue #12 titled "Add Feature Sling Model" → `issue-12-add-feature-sling-model`
   - Keep the description concise (3–5 words max), lowercase, hyphens only
3. **Push the branch to remote** immediately so it exists on origin before any work begins
4. **Do all work on that branch** — never modify files on `main`
5. **Stop after completing the work** — do NOT commit or push; leave that to the developer
6. **Create a PR** using `gh pr create` with the following description format:

```
Fix #{issue-number}
```

```bash
# Example for issue #12 "Add Feature Sling Model"
git fetch origin
git checkout -b issue-12-add-feature-sling-model
git push -u origin issue-12-add-feature-sling-model
# ... do the work ...
npm run lint          # must pass before committing
gh pr create --title "Add testimonial block" --body "Fix #12"
```

## Branch Naming Rules

- Always prefix with `issue-{number}-`
- Description in kebab-case, derived from the issue title
- Lowercase letters and hyphens only — no uppercase, no spaces, no underscores
- Keep the description part (after `issue-{number}-`) **under 24 characters**

## TypeScript Configuration

- **Target**: ES5 (legacy browser support)
- **Module**: ES6
- **Source Maps**: Enabled in dev, disabled in prod
- **Path Resolution**: `tsconfig-paths-webpack-plugin` uses `baseUrl: ../ui.frontend`
- **ESLint**: TypeScript parser with recommended rules; max line length 120

## Configuration Files Reference

- `.babelrc` - Babel presets: TypeScript, class properties, object spread
- `.eslintrc.js` - TypeScript ESLint with rule overrides
- `tsconfig.json` - TypeScript compiler options
- `webpack.common.js` - Shared webpack rules and plugins
- `webpack.dev.js` - Dev mode with HtmlWebpackPlugin and dev server proxy
- `webpack.prod.js` - Production mode with Terser and CssMinimizer
- `clientlib.config.js` - aem-clientlib-generator configuration

## Important Notes

### Accessibility & Best Practices

From README.md - this project demonstrates:
- Semantic HTML (header, nav, section, article)
- Proper alt text for images
- ARIA labels for links, especially "opens in new tab"
- Author-friendly dialogs with field titles, descriptions, Tab grouping, placeholders

### Dispatcher Caching Strategy

From README.md bonus section:
- TTL: 5 minutes for .html content (configurable via enableTTL)
- Long cache: 24 hours for .js and .css
- Invalidation: Author publish triggers cache invalidation to all publish instances

### Multi-Brand Site Structure Assumption

```
/conf/
├── brand-a/settings/wcm/templates/
├── brand-b/settings/wcm/templates/
├── brand-c/settings/wcm/templates/
└── brand-d/settings/wcm/templates/
```

Each brand has its own templates; component is shared; theming is CSS-based per brand.

### Cloud Manager Quality Gates

Expected quality checks (per README.md):
- **Code Coverage**: Unit test coverage report generated via `mvn clean test`
- **SonarQube**: Download Adobe Full SonarQube rules; install in IDE for pre-merge validation
- **Custom Rules**: Cloud Manager pipeline may include additional AEM-specific checks
