set -e

mongosh <<EOF
use test

db.createUser({
  user: 'tester',
  pwd: 'tester',
  roles: [{ role: 'readWrite', db: 'test' }],
  });

  EOF

