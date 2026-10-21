# Acceptance scripts

Only read-only candidate-state validation is materialized initially. VM lifecycle/deploy/accept/reject operations belong to the trusted local controller and require its approved credential/isolation boundary; they must not be executable by untrusted PR code.
