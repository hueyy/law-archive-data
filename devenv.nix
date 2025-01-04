{ pkgs, lib, config, inputs, ... }:

{
  packages = [
    pkgs.tzdata
    pkgs.git
    pkgs.babashka
    pkgs.poppler_utils
    pkgs.ghostscript
    pkgs.tesseract
    pkgs.sqlite
    pkgs.flyctl
  ];

  languages = {
    javascript = {
      enable = true;
      directory = "./app";
      pnpm = {
        enable = true;
        install.enable = true;
      };
    };

    python = {
      enable = true;
      poetry = {
        enable = true;
        install.enable = true;
        activate.enable = true;
      };
    };
  };

  scripts.dev-frontend.exec = ''cd app && pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --watch'';
  scripts.build-frontend.exec = ''cd app && pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --minify'';

  scripts.build-db.exec = ''bb --main scripts.build-db'';
  scripts.dev-datasette.exec = ''bb ./scripts/dev_docker.bb'';

  scripts.fetch-fc-judgments.exec = ''bb --main input.judgments.fc-judgments'';
  scripts.fetch-hearings.exec = ''bb --main input.hearings.get-hearings'';
  scripts.fetch-lss-dt-reports.exec = ''bb --main input.lss.dt-reports'';
  scripts.fetch-pdpc-decisions.exec = ''bb --main input.pdpc.decisions'';
  scripts.fetch-pdpc-undertakings.exec = ''bb --main input.pdpc.undertakings'';
  scripts.fetch-sal-specialists.exec = ''bb --main input.specialists.sal-specialists'';
  scripts.fetch-sc.exec = ''bb --main input.sc.run'';
  scripts.fetch-stc-judgments.exec = ''bb --main input.judgments.stc-judgments'';
  scripts.fetch-telco-fbo.exec = ''bb --main input.telco.fbo'';

  scripts.automated-git-push.exec = ''
    git config user.name "Automated update"
    git config user.email "actions@users.noreply.github.com"
    git add -A
    timestamp=''$(TZ='Asia/Singapore' date)
    git commit -m "Latest $@ data: ''${timestamp}" || exit 0
    git pull --rebase
    git push
  '';

  scripts.cd-deploy.exec = ''flyctl deploy --remote-only --detach'';

  enterTest = ''
    cljfmt check --file-pattern input/*.bb && \
    bb --main test.input.utils.date-test
  '';

  git-hooks.hooks = {
    mdsh.enable = true;
    cljfmt.enable = true;
  };
}
